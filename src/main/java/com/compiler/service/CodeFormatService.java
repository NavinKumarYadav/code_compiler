package com.compiler.service;


import org.springframework.stereotype.Service;

@Service
public class CodeFormatService {

    public String formatCode(String code, String language){
        if (code == null || code.trim().isEmpty()){
            return code;
        }

        switch (language.toLowerCase()){
            case "java":
                return formatJavaCode(code);
            case "python":
                return formatPythonCode(code);
            case "javascript":
                return formatJavaScriptCode(code);
            default:
                return code;
        }
    }

    private String formatJavaCode(String code) {
        return code.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*\\{\\s*", " { ")
                .replaceAll("\\s*\\}\\s*", " } ")
                .replaceAll("\\s*;\\s*", "; ");
    }

    private String formatPythonCode(String code) {
        return code.trim();
    }
    private String formatJavaScriptCode(String code) {
        return code.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*\\{\\s*", " { ")
                .replaceAll("\\s*\\}\\s*", " } ")
                .replaceAll("\\s*;\\s*", "; ");
    }

}
