package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.dto.ExecutionLimits;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final CodeValidationService validationService;
    private final Judge0Service judge0Service;

    public ExecutionResponse executeCode(ExecutionRequest request, String userIdentifier, HttpServletRequest httpRequest) {
        log.info("Executing code for user: {}, language: {}", userIdentifier, request.getLanguage());

        try {
            // 1. Get execution limits for this user
            ExecutionLimits limits = validationService.getExecutionLimits(userIdentifier);

            // 2. Apply execution limits to the request (if needed)
            // Your existing Judge0Service already handles limits through configuration

            // 3. Execute code using your existing Judge0Service
            ExecutionResponse response = judge0Service.executeCode(request, httpRequest);

            log.info("Code execution completed for user: {}, success: {}", userIdentifier,
                    response.getError() == null || response.getError().isEmpty());
            return response;

        } catch (Exception e) {
            log.error("Code execution failed for user: {}", userIdentifier, e);
            return ExecutionResponse.error("Execution failed: " + e.getMessage());
        }
    }

    // Overloaded method without HttpServletRequest for backward compatibility
    public ExecutionResponse executeCode(ExecutionRequest request, String userIdentifier) {
        // Create a simple mock HttpServletRequest or use null
        // Note: This might not work for all features like session tracking
        return executeCode(request, userIdentifier, null);
    }
}