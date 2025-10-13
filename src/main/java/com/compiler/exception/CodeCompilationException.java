package com.compiler.exception;

public class CodeCompilationException extends RuntimeException {
    public CodeCompilationException(String message) {
        super(message);
    }

    public CodeCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}