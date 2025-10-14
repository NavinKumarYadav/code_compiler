package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionLimits;
import com.compiler.exception.SecurityValidationException;
import com.compiler.security.CodeSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeValidationService {

    private final CodeSanitizer codeSanitizer;
    private final RateLimitService rateLimitService;
    private final ResourceLimitService resourceLimitService;

    public void validateExecutionRequest(ExecutionRequest request, String userIdentifier) {
        log.info("Validating execution request for user: {}", userIdentifier);

        if (!rateLimitService.isAllowed(userIdentifier)) {
            log.warn("Rate limit exceeded for user: {}", userIdentifier);
            throw new SecurityValidationException("Rate limit exceeded. Please try again later.");
        }

        if (!resourceLimitService.checkCodeSize(request.getCode())) {
            log.warn("Code size limit exceeded for user: {}", userIdentifier);
            throw new SecurityValidationException("Code size exceeds maximum allowed limit.");
        }

        if (!resourceLimitService.checkInputSize(request.getInput())) {
            log.warn("Input size limit exceeded for user: {}", userIdentifier);
            throw new SecurityValidationException("Input size exceeds maximum allowed limit.");
        }

        codeSanitizer.validateLanguage(request.getLanguage());
        log.debug("Language validated: {}", request.getLanguage());

        codeSanitizer.validateInput(request.getInput());
        log.debug("Input validated");

        if (!codeSanitizer.isCodeSafe(request.getCode(), request.getLanguage())) {
            log.warn("Security violation detected in code for user: {}", userIdentifier);
            throw new SecurityValidationException("Code contains potentially dangerous operations.");
        }
        String sanitizedCode = codeSanitizer.sanitizeCode(request.getCode(), request.getLanguage());
        request.setCode(sanitizedCode);

        log.info("Code sanitized successfully. User: {}, Language: {}", userIdentifier, request.getLanguage());
    }

    public int getRemainingRequests(String userIdentifier) {
        return rateLimitService.getRemainingRequests(userIdentifier);
    }

    public ExecutionLimits getExecutionLimits(String userIdentifier) {
        return resourceLimitService.getLimitsForUser(userIdentifier);
    }
}