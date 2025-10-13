package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
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

    public void validateExecutionRequest(ExecutionRequest request, String userIdentifier) {
        log.info("Validating execution request for user: {}", userIdentifier);

        if (!rateLimitService.isAllowed(userIdentifier)) {
            log.warn("Rate limit exceeded for user: {}", userIdentifier);
            throw new SecurityException("Rate limit exceeded. Please try again later.");
        }

        codeSanitizer.validateLanguage(request.getLanguage());
        log.debug("Language validated: {}", request.getLanguage());

        codeSanitizer.validateInput(request.getInput());
        log.debug("Input validated");

        String originalCode = request.getCode();
        String sanitizedCode = codeSanitizer.sanitizeCode(request.getCode(), request.getLanguage());
        request.setCode(sanitizedCode);

        log.info("Code sanitized successfully. User: {}, Language: {}", userIdentifier, request.getLanguage());
    }

    public int getRemainingRequests(String userIdentifier) {
        return (int) rateLimitService.getRemainingRequests(userIdentifier);
    }
}