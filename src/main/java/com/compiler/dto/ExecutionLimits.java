package com.compiler.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionLimits {
    private final long timeoutMs;
    private final long maxMemoryMb;
    private final long maxOutputBytes;

    public ExecutionLimits() {
        this.timeoutMs = 10000;
        this.maxMemoryMb = 100;
        this.maxOutputBytes = 10 * 1024;
    }

    public ExecutionLimits(long timeoutMs, long maxMemoryMb, long maxOutputBytes) {
        this.timeoutMs = timeoutMs;
        this.maxMemoryMb = maxMemoryMb;
        this.maxOutputBytes = maxOutputBytes;
    }
}