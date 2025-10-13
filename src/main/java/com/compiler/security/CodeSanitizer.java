package com.compiler.security;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CodeSanitizer {

    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
            "Runtime.getRuntime()", "ProcessBuilder", "System.exit",
            "FileInputStream", "FileOutputStream", "exec(", "cmd.exe",
            "/bin/sh", "bash", "powershell", "eval(", "System.set",
            "java.lang.Runtime", "java.lang.Process", "java.io.File",
            "java.net.Socket", "java.net.ServerSocket", "System.in",
            "Thread.sleep", "while(true)", "for(;;)", "ProcessBuilder",
            "Runtime.exec", "UNIXProcess", "WindowsProcess"
    );

    private static final List<String> ADDITIONAL_DANGEROUS_PATTERNS = Arrays.asList(
            "java.nio.file", "Files.walk", "Files.delete",
            "new URL(", "HttpURLConnection", "URLConnection",
            "ScriptEngine", "GroovyShell", "JavaScript",
            "defineClass", "setAccessible", "getDeclared",
            "Unsafe", "sun.misc", "reflect.", "MethodHandle",
            "VarHandle", "AtomicReference", "Unsafe"
    );

    private static final List<String> JAVA_DANGEROUS = Arrays.asList(
            "Runtime.getRuntime()", "ProcessBuilder", "System.exit",
            "exec(", "java.lang.Process", "java.net", "java.nio",
            "FileInputStream", "FileOutputStream", "Unsafe",
            "setAccessible", "getDeclared", "defineClass"
    );

    private static final List<String> PYTHON_DANGEROUS = Arrays.asList(
            "import os", "import subprocess", "eval(", "exec(", "__import__",
            "open(", "file(", "compile(", "input(", "raw_input",
            "os.system", "os.popen", "subprocess.call", "subprocess.Popen",
            "commands.getoutput", "pty.spawn", "pdb.set_trace"
    );

    private static final List<String> CPP_DANGEROUS = Arrays.asList(
            "system(", "exec(", "fork(", "popen(", "fopen(", "fstream",
            "ifstream", "ofstream", "socket(", "connect(", "chmod(",
            "chown(", "unistd.h", "sys/mman.h", "sys/wait.h"
    );

    private static final List<String> JS_DANGEROUS = Arrays.asList(
            "eval(", "Function(", "setTimeout(", "setInterval(", "exec(",
            "require('child_process')", "require('fs')", "process.",
            "child_process.", "fs.", "vm.", "module.", "__dirname",
            "__filename", "global.", "window.", "document."
    );

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+.*");
    private static final Pattern SYSTEM_CALL_PATTERN = Pattern.compile("System\\s*\\.\\s*[a-zA-Z]");

    private static final Pattern BASE64_PATTERN = Pattern.compile("[A-Za-z0-9+/]{40,}={0,2}");

    private static final Pattern HEX_PATTERN = Pattern.compile("\\\\x[0-9A-Fa-f]{2}");

    private static final int MAX_CODE_LENGTH = 10000;
    private static final int MAX_INPUT_LENGTH = 1000;

    private static final List<String> ALLOWED_LANGUAGES = Arrays.asList(
            "java", "python", "cpp", "c", "javascript", "go"
    );

    private final ConcurrentHashMap<String, AtomicInteger> codePatternCount = new ConcurrentHashMap<>();

    public void validateLanguage(String language) {
        if (language == null || !ALLOWED_LANGUAGES.contains(language.toLowerCase())) {
            throw new SecurityException("Unsupported language: " + language +
                    ". Supported languages: " + String.join(", ", ALLOWED_LANGUAGES));
        }
    }

    public String sanitizeCode(String code, String language) {
        if (code == null || code.trim().isEmpty()) {
            throw new SecurityException("Code cannot be empty");
        }

        if (code.length() > MAX_CODE_LENGTH) {
            throw new SecurityException("Code exceeds maximum length of " + MAX_CODE_LENGTH + " characters");
        }

        checkForEncodedCode(code);

        checkForDeepRecursion(code, language);

        return switch (language.toLowerCase()) {
            case "java" -> sanitizeJavaCode(code);
            case "python" -> sanitizePythonCode(code);
            case "cpp", "c" -> sanitizeCppCode(code);
            case "javascript" -> sanitizeJavaScriptCode(code);
            case "go" -> sanitizeGoCode(code);
            default -> code;
        };
    }

    public void validateInput(String input) {
        if (input != null && input.length() > MAX_INPUT_LENGTH) {
            throw new SecurityException("Input too large. Maximum " + MAX_INPUT_LENGTH + " characters allowed");
        }

        if (input != null) {
            checkForDangerousPatterns(input, "Input");
        }
    }

    public boolean isCodeSafe(String code, String language) {
        try {
            sanitizeCode(code, language);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    private String sanitizeJavaCode(String code) {
        String sanitized = removeDangerousImports(code);

        checkForDangerousKeywords(sanitized, JAVA_DANGEROUS, "Java");

        if (containsInfiniteLoop(sanitized)) {
            throw new SecurityException("Potential infinite loop detected");
        }

        checkMemoryExhaustion(sanitized);

        checkForReflection(sanitized);

        return sanitized;
    }

    private String sanitizePythonCode(String code) {
        checkForDangerousKeywords(code, PYTHON_DANGEROUS, "Python");

        if (code.contains("__class__") || code.contains("__bases__") || code.contains("__subclasses__")) {
            throw new SecurityException("Potential Python introspection attack detected");
        }

        return code;
    }

    private String sanitizeCppCode(String code) {
        checkForDangerousKeywords(code, CPP_DANGEROUS, "C++");

        // NEW: Check for inline assembly
        if (code.contains("asm(") || code.contains("__asm__")) {
            throw new SecurityException("Inline assembly not allowed");
        }

        return code;
    }

    private String sanitizeJavaScriptCode(String code) {
        checkForDangerousKeywords(code, JS_DANGEROUS, "JavaScript");

        if (code.contains("__proto__") || code.contains("constructor.prototype")) {
            throw new SecurityException("Potential prototype pollution attack detected");
        }

        return code;
    }

    private String sanitizeGoCode(String code) {
        List<String> goDangerous = Arrays.asList(
                "os.Exec", "syscall", "exec.Command", "io/ioutil",
                "net/http", "os.Open", "os.Create", "runtime."
        );

        checkForDangerousKeywords(code, goDangerous, "Go");
        return code;
    }

    private String removeDangerousImports(String code) {
        return IMPORT_PATTERN.matcher(code).replaceAll("");
    }

    private void checkForDangerousKeywords(String code, List<String> dangerousList, String language) {
        String lowerCode = code.toLowerCase();

        for (String keyword : dangerousList) {
            if (lowerCode.contains(keyword.toLowerCase())) {
                throw new SecurityException(
                        String.format("Dangerous %s operation detected: %s", language, keyword)
                );
            }
        }

        for (String keyword : DANGEROUS_KEYWORDS) {
            if (lowerCode.contains(keyword.toLowerCase())) {
                throw new SecurityException("Dangerous operation detected: " + keyword);
            }
        }
    }

    private boolean containsInfiniteLoop(String code) {
        String lowerCode = code.toLowerCase();
        return (lowerCode.contains("while(true)") ||
                lowerCode.contains("for(;;)") ||
                lowerCode.contains("while(1)") ||
                lowerCode.contains("for(;;)") ||
                lowerCode.contains("while (true)") ||
                lowerCode.contains("for (;;)"));
    }

    private void checkMemoryExhaustion(String code) {
        if (code.contains("new byte[") && code.contains("1000000")) {
            throw new SecurityException("Potential memory exhaustion attack detected");
        }

        if (code.matches("new\\s+\\w+\\[\\s*\\d{6,}\\s*\\]")) {
            throw new SecurityException("Large array allocation detected - potential memory attack");
        }
    }

    private void checkForReflection(String code) {
        if (code.contains("getDeclared") || code.contains("setAccessible(true)")) {
            throw new SecurityException("Reflection-based security bypass detected");
        }
    }

    private void checkForEncodedCode(String code) {
        if (BASE64_PATTERN.matcher(code).find()) {
            throw new SecurityException("Base64 encoded content detected - potential obfuscation");
        }

        if (HEX_PATTERN.matcher(code).find()) {
            throw new SecurityException("Hexadecimal encoded content detected - potential obfuscation");
        }
    }

    private void checkForDeepRecursion(String code, String language) {
        // Simple check for multiple recursive calls
        if (countOccurrences(code, language.equals("java") ? "public static" : "def ") > 10) {
            throw new SecurityException("Too many function definitions - potential code complexity attack");
        }
    }

    private void checkForDangerousPatterns(String content, String context) {
        for (String pattern : ADDITIONAL_DANGEROUS_PATTERNS) {
            if (content.toLowerCase().contains(pattern.toLowerCase())) {
                throw new SecurityException("Dangerous pattern detected in " + context + ": " + pattern);
            }
        }
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}