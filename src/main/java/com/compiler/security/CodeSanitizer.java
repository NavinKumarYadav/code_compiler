package com.compiler.security;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CodeSanitizer {

    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
            "Runtime.getRuntime()", "ProcessBuilder", "System.exit",
            "FileInputStream", "FileOutputStream", "exec(", "cmd.exe",
            "/bin/sh", "bash", "powershell", "eval(", "System.set",
            "java.lang.Runtime", "java.lang.Process", "java.io.File",
            "java.net.Socket", "java.net.ServerSocket", "System.in",
            "Thread.sleep", "while(true)", "for(;;)"
    );

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+.*");

    private static final int MAX_CODE_LENGTH = 10000;
    private static final int MAX_INPUT_LENGTH = 1000;

    private static final List<String> ALLOWED_LANGUAGES = Arrays.asList(
            "java", "python", "cpp", "c", "javascript", "go"
    );

    public void validateLanguage(String language) {
        if (language == null || !ALLOWED_LANGUAGES.contains(language.toLowerCase())) {
            throw new SecurityException("Unsupported language: " + language +
                    ". Supported languages: " + String.join(", ", ALLOWED_LANGUAGES));
        }
    }

    public String sanitizeCode(String code, String language){
        if(code == null || code.trim().isEmpty()){
            throw new SecurityException("Code cannot be empty");
        }

        if (code.length() > MAX_CODE_LENGTH) {
            throw new SecurityException("Code exceeds maximum length of " + MAX_CODE_LENGTH + " characters");
        }

        return switch (language.toLowerCase()){
            case "java" -> sanitizeJavaCode(code);
            case "python" -> sanitizePythonCode(code);
            case "cpp", "c" -> sanitizeCppCode(code);
            case "javascript" -> sanitizeJavaScriptCode(code);
            default -> code;
        };
    }

    public void validateInput(String input){
        if (input != null && input.length() > MAX_INPUT_LENGTH) {
            throw new SecurityException("Input too large. Maximum " + MAX_INPUT_LENGTH + " characters allowed");
        }
    }

    private String sanitizeJavaCode(String code){
        String sanitized = removeDangerousImports(code);

        checkForDangerousKeywords(sanitized);

        if (containsInfiniteLoop(sanitized)) {
            throw new SecurityException("Potential infinite loop detected");
        }

        return sanitized;
    }

    private String sanitizePythonCode(String code) {
        List<String> pythonDangerous = Arrays.asList(
                "import os", "import subprocess", "eval(", "exec(", "__import__",
                "open(", "file(", "compile(", "input(", "raw_input"
        );

        for (String keyword : pythonDangerous) {
            if (code.contains(keyword)) {
                throw new SecurityException("Dangerous Python operation detected: " + keyword);
            }
        }

        return code;
    }

    private String sanitizeCppCode(String code) {
        List<String> cppDangerous = Arrays.asList(
                "system(", "exec(", "fork(", "popen(", "fopen(", "fstream",
                "ifstream", "ofstream", "socket(", "connect("
        );

        for (String keyword : cppDangerous) {
            if (code.contains(keyword)) {
                throw new SecurityException("Dangerous C++ operation detected: " + keyword);
            }
        }

        return code;
    }

    private String sanitizeJavaScriptCode(String code) {
        List<String> jsDangerous = Arrays.asList(
                "eval(", "Function(", "setTimeout(", "setInterval(", "exec(",
                "require('child_process')", "require('fs')", "process."
        );

        for (String keyword : jsDangerous) {
            if (code.contains(keyword)) {
                throw new SecurityException("Dangerous JavaScript operation detected: " + keyword);
            }
        }

        return code;
    }

    private String removeDangerousImports(String code) {
        return IMPORT_PATTERN.matcher(code).replaceAll("");
    }

    private void checkForDangerousKeywords(String code) {
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (code.contains(keyword)) {
                throw new SecurityException("Dangerous operation detected: " + keyword);
            }
        }
    }

    private boolean containsInfiniteLoop(String code) {
        String lowerCode = code.toLowerCase();
        return (lowerCode.contains("while(true)") ||
                lowerCode.contains("for(;;)") ||
                lowerCode.contains("while(1)") ||
                lowerCode.contains("for(;;)"));
    }
}