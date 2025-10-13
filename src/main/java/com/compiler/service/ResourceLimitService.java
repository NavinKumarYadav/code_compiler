package com.compiler.service;

import com.compiler.dto.ExecutionLimits;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceLimitService {

    private static final int MAX_CODE_SIZE_BYTES = 100 * 1024;
    private static final int MAX_EXECUTION_TIME_MS = 10000;
    private static final int MAX_MEMORY_MB = 100;
    private static final int MAX_OUTPUT_SIZE_BYTES = 10 * 1024;

    public boolean checkCodeSize(String code) {
        if (code == null) return false;
        int codeSize = code.getBytes().length;
        boolean isValid = codeSize <= MAX_CODE_SIZE_BYTES;

        if (!isValid) {
            log.warn("Code size exceeded: {} bytes (max: {})", codeSize, MAX_CODE_SIZE_BYTES);
        }

        return isValid;
    }

    public boolean checkInputSize(String input) {
        if (input == null) return true;
        int inputSize = input.getBytes().length;
        boolean isValid = inputSize <= (10 * 1024);

        if (!isValid) {
            log.warn("Input size exceeded: {} bytes (max: {})", inputSize, 10 * 1024);
        }

        return isValid;
    }

    public ExecutionLimits getLimitsForUser(String userIdentifier) {
        return ExecutionLimits.builder()
                .timeoutMs(MAX_EXECUTION_TIME_MS)
                .maxMemoryMb(MAX_MEMORY_MB)
                .maxOutputBytes(MAX_OUTPUT_SIZE_BYTES)
                .build();
    }
}