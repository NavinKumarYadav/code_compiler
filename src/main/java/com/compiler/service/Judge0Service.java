package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.entity.CodeSubmission;
import com.compiler.entity.User;
import com.compiler.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Judge0Service {

    @Value("${judge0.api.key}")
    private String rapidApiKey;

    @Value("${judge0.api.url}")
    private String judge0BaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SubmissionHistoryService submissionHistoryService;

    private final Map<String, Integer> LANGUAGE_IDS = createLanguageMap();

    private final RateLimitService rateLimitService;

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final CodeFormatService codeFormatService;

    private final CodeValidationService codeValidationService;

    public Judge0Service(RestTemplate restTemplate, ObjectMapper objectMapper,
                         SubmissionHistoryService submissionHistoryService,
                         RateLimitService rateLimitService, UserService userService,
                         JwtUtil jwtUtil, CodeFormatService codeFormatService,
                         CodeValidationService codeValidationService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.submissionHistoryService = submissionHistoryService;
        this.rateLimitService = rateLimitService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.codeFormatService = codeFormatService;
        this.codeValidationService = codeValidationService;
    }

    private Map<String, Integer> createLanguageMap() {
        Map<String, Integer> languages = new HashMap<>();
        languages.put("java", 62);
        languages.put("python", 71);
        languages.put("cpp", 54);
        languages.put("c", 50);
        languages.put("javascript", 63);
        languages.put("csharp", 51);
        languages.put("ruby", 72);
        languages.put("go", 60);
        languages.put("rust", 73);
        languages.put("kotlin", 78);
        languages.put("swift", 83);
        return languages;
    }

    public ExecutionResponse executeCode(ExecutionRequest request) {
        return executeCode(request, null);
    }

    public ExecutionResponse executeCode(ExecutionRequest request, HttpServletRequest httpRequest) {

        log.info("=== JUDGE0 SECURE EXECUTION ===");

        if (request == null) {
            return ExecutionResponse.error("Execution request cannot be null");
        }

        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            return ExecutionResponse.error("Language must be specified");
        }

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ExecutionResponse.error("Code cannot be empty");
        }

        log.info("Language: {}", request.getLanguage());

        String clientId = getClientIdentifier(httpRequest);

        try {
            codeValidationService.validateExecutionRequest(request, clientId);
            log.info("Security validation passed for client: {}", clientId);

            if (rapidApiKey == null || rapidApiKey.trim().isEmpty()) {
                return ExecutionResponse.error("Judge0 API key is not configured");
            }

            Integer languageId = LANGUAGE_IDS.get(request.getLanguage().toLowerCase());
            log.debug("Language ID resolved: {} for language: {}", languageId, request.getLanguage());

            if (languageId == null) {
                return ExecutionResponse.error("Unsupported language: " + request.getLanguage());
            }

            Map<String, Object> submission = new HashMap<>();

            String formattedCode = codeFormatService.formatCode(request.getCode(), request.getLanguage());

            submission.put("source_code", formattedCode);
            submission.put("language_id", languageId);
            submission.put("stdin", request.getInput() != null ? request.getInput() : "");

            if (request.getExpectedOutput() != null && !request.getExpectedOutput().trim().isEmpty()) {
                submission.put("expected_output", request.getExpectedOutput());
            }

            log.debug("Submission prepared for language: {}", request.getLanguage());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-RapidAPI-Key", rapidApiKey);
            headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

            String submissionJson = objectMapper.writeValueAsString(submission);
            log.debug("Sending request to Judge0 API");

            HttpEntity<String> entity = new HttpEntity<>(submissionJson, headers);

            String submissionUrl = judge0BaseUrl + "/submissions?base64_encoded=false&wait=true";
            log.debug("Making request to: {}", submissionUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    submissionUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.debug("Response Status: {}", response.getStatusCode());

            ExecutionResponse executionResponse;

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                executionResponse = mapToExecutionResponse(responseBody, request.getExpectedOutput());
                log.info("Code execution completed successfully for client: {}", clientId);
            } else {
                executionResponse = ExecutionResponse.error("Unexpected response from Judge0: " + response.getStatusCode());
                log.warn("Unexpected response from Judge0: {}", response.getStatusCode());
            }

            saveSubmissionHistory(request, executionResponse, httpRequest);
            return executionResponse;

        } catch (SecurityException e) {
            log.warn("Security violation detected for client {}: {}", clientId, e.getMessage());
            ExecutionResponse securityResponse = ExecutionResponse.error("Security violation: " + e.getMessage());
            saveSubmissionHistory(request, securityResponse, httpRequest);
            return securityResponse;

        } catch (HttpClientErrorException e) {
            log.error("Judge0 API HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            ExecutionResponse errorResponse;

            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorResponse = ExecutionResponse.error("Authentication failed. Please check your Judge0 API key.");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                errorResponse = ExecutionResponse.error("Rate limit exceeded. Please try again later.");
            } else if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                errorResponse = ExecutionResponse.error("Invalid request format: " + e.getResponseBodyAsString());
            } else {
                errorResponse = ExecutionResponse.error("Judge0 API error: " + e.getStatusCode());
            }

            saveSubmissionHistory(request, errorResponse, httpRequest);
            return errorResponse;

        } catch (Exception e) {
            log.error("Judge0 service error for client {}: {}", clientId, e.getMessage(), e);
            ExecutionResponse errorResponse = ExecutionResponse.error("Execution failed: " + e.getMessage());
            saveSubmissionHistory(request, errorResponse, httpRequest);
            return errorResponse;
        }
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !(authentication instanceof AnonymousAuthenticationToken) &&
                    userService != null) {
                return userService.findByUsername(authentication.getName());
            }
        } catch (Exception e) {
            log.debug("User service not available: {}", e.getMessage());
        }
        return null;
    }

    private void saveSubmissionHistory(ExecutionRequest request, ExecutionResponse response,
                                       HttpServletRequest httpRequest) {
        try {
            log.info("🔄 SAVE SUBMISSION - Starting save process for language: {}", request.getLanguage());

            String sessionId = "no-session";
            if (httpRequest != null && httpRequest.getSession() != null) {
                sessionId = httpRequest.getSession().getId();
                log.info("📱 Session ID: {}", sessionId);
            } else {
                log.warn("⚠️ No HTTP request or session available");
            }

            User currentUser = getCurrentUser();
            log.info("👤 Current user: {}", currentUser != null ? currentUser.getUsername() : "anonymous");

            CodeSubmission savedSubmission = submissionHistoryService.saveSubmissionWithResult(
                    request.getCode(),
                    request.getLanguage(),
                    response.getOutput(),
                    response.getError(),
                    response.getStatus(),
                    response.getExecutionTime(),
                    response.getMemoryUsed(),
                    response.getIsCorrect(),
                    currentUser,
                    sessionId
            );

            log.info("✅ SUBMISSION SAVED SUCCESSFULLY - ID: {}, Language: {}",
                    savedSubmission.getId(), savedSubmission.getLanguage());

        } catch (Exception e) {
            log.error("❌ FAILED TO SAVE SUBMISSION: {}", e.getMessage(), e);
        }
    }

    private ExecutionResponse mapToExecutionResponse(Map<String, Object> response, String expectedOutput) {
        ExecutionResponse result = new ExecutionResponse();

        if (response != null) {
            if (response.containsKey("stdout") && response.get("stdout") != null) {
                result.setOutput(response.get("stdout").toString().trim());
            } else if (response.containsKey("compile_output") && response.get("compile_output") != null) {
                result.setOutput(response.get("compile_output").toString().trim());
            }

            if (response.containsKey("stderr") && response.get("stderr") != null) {
                String stderr = response.get("stderr").toString().trim();
                if (!stderr.isEmpty()) {
                    result.setError(stderr);
                }
            }

            if (response.containsKey("time") && response.get("time") != null) {
                try {
                    result.setExecutionTime(Double.parseDouble(response.get("time").toString()));
                } catch (NumberFormatException e) {
                    result.setExecutionTime(0.0);
                    log.debug("Failed to parse execution time: {}", response.get("time"));
                }
            }

            if (response.containsKey("memory") && response.get("memory") != null) {
                try {
                    result.setMemoryUsed(Double.parseDouble(response.get("memory").toString()));
                } catch (NumberFormatException e) {
                    result.setMemoryUsed(0.0);
                    log.debug("Failed to parse memory usage: {}", response.get("memory"));
                }
            }

            if (response.containsKey("status")) {
                Object statusObj = response.get("status");
                if (statusObj instanceof Map) {
                    Map<String, Object> status = (Map<String, Object>) statusObj;
                    if (status.containsKey("description")) {
                        result.setStatus(status.get("description").toString());
                    }
                }
            }

            if (expectedOutput != null && !expectedOutput.isEmpty()) {
                String actualOutput = result.getOutput() != null ? result.getOutput().trim() : "";
                result.setIsCorrect(actualOutput.equals(expectedOutput.trim()));
            }

            if (result.getStatus() == null) {
                result.setStatus("Completed");
            }
        }

        return result;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        if (request == null) {
            return "anonymous-client";
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                return jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.debug("Failed to extract user from token: {}", e.getMessage());
            }
        }
        return request.getSession().getId();
    }

    public Map<String, String> getSupportedLanguages() {
        Map<String, String> languages = new HashMap<>();
        languages.put("java", "Java (OpenJDK 13.0.1)");
        languages.put("python", "Python (3.8.1)");
        languages.put("cpp", "C++ (GCC 9.2.0)");
        languages.put("c", "C (GCC 9.2.0)");
        languages.put("javascript", "JavaScript (Node.js 12.14.0)");
        languages.put("csharp", "C# (Mono 6.6.0)");
        languages.put("ruby", "Ruby (2.7.0)");
        languages.put("go", "Go (1.13.5)");
        languages.put("rust", "Rust (1.40.0)");
        languages.put("kotlin", "Kotlin (1.3.70)");
        languages.put("swift", "Swift (5.2.3)");
        return languages;
    }
}