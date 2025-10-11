package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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

    public Judge0Service(RestTemplate restTemplate, ObjectMapper objectMapper,
                         SubmissionHistoryService submissionHistoryService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.submissionHistoryService = submissionHistoryService;
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
        System.out.println("=== JUDGE0 DEBUG ===");
        System.out.println("API Key present: " + (rapidApiKey != null && !rapidApiKey.isEmpty()));
        System.out.println("Judge0 URL: " + judge0BaseUrl);
        System.out.println("Language: " + request.getLanguage());
        System.out.println("Code: " + (request.getCode() != null ? request.getCode() : "NULL"));

        if (rapidApiKey == null || rapidApiKey.trim().isEmpty()) {
            return ExecutionResponse.error("Judge0 API key is not configured");
        }

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ExecutionResponse.error("Source code cannot be empty");
        }

        Integer languageId = LANGUAGE_IDS.get(request.getLanguage());
        System.out.println("Language ID resolved: " + languageId);

        if (request.getLanguage() == null || languageId == null) {
            return ExecutionResponse.error("Unsupported language: " + request.getLanguage());
        }

        Map<String, Object> submission = new HashMap<>();
        submission.put("source_code", request.getCode());
        submission.put("language_id", languageId);
        submission.put("stdin", request.getInput() != null ? request.getInput() : "");

        if (request.getExpectedOutput() != null && !request.getExpectedOutput().trim().isEmpty()) {
            submission.put("expected_output", request.getExpectedOutput());
        }

        System.out.println("Submission Map: " + submission);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

        try {
            String submissionJson = objectMapper.writeValueAsString(submission);
            System.out.println("JSON being sent: " + submissionJson);

            HttpEntity<String> entity = new HttpEntity<>(submissionJson, headers);

            String submissionUrl = judge0BaseUrl + "/submissions?base64_encoded=false&wait=true";
            System.out.println("Making request to: " + submissionUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    submissionUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Raw Response: " + response.getBody());

            ExecutionResponse executionResponse;

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                executionResponse = mapToExecutionResponse(responseBody, request.getExpectedOutput());
            } else {
                executionResponse = ExecutionResponse.error("Unexpected response from Judge0: " + response.getStatusCode());
            }

            saveSubmissionHistory(request, executionResponse, httpRequest);

            return executionResponse;

        } catch (HttpClientErrorException e) {
            System.out.println("HTTP Error: " + e.getStatusCode());
            System.out.println("Error Response: " + e.getResponseBodyAsString());

            ExecutionResponse errorResponse;

            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorResponse = ExecutionResponse.error("Authentication failed. Please check your Judge0 API key.");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                errorResponse = ExecutionResponse.error("Rate limit exceeded. Please try again later.");
            } else if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                errorResponse = ExecutionResponse.error("Invalid request format: " + e.getResponseBodyAsString());
            } else {
                errorResponse = ExecutionResponse.error("Judge0 API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            }

            saveSubmissionHistory(request, errorResponse, httpRequest);
            return errorResponse;

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            ExecutionResponse errorResponse = ExecutionResponse.error("Execution failed: " + e.getMessage());
            saveSubmissionHistory(request, errorResponse, httpRequest);
            return errorResponse;
        }
    }

    private void saveSubmissionHistory(ExecutionRequest request, ExecutionResponse response, HttpServletRequest httpRequest) {
        try {
            String sessionId = (httpRequest != null && httpRequest.getSession() != null) ?
                    httpRequest.getSession().getId() : "anonymous";

            submissionHistoryService.saveSubmissionWithResult(
                    request.getCode(),
                    request.getLanguage(),
                    response.getOutput(),
                    response.getError(),
                    response.getStatus(),
                    response.getExecutionTime(),
                    response.getMemoryUsed(),
                    response.getIsCorrect(),
                    null,
                    sessionId
            );

            System.out.println("✅ Submission history saved successfully for session: " + sessionId);
        } catch (Exception e) {
            System.out.println("❌ Failed to save submission history: " + e.getMessage());
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
                }
            }

            if (response.containsKey("memory") && response.get("memory") != null) {
                try {
                    result.setMemoryUsed(Double.parseDouble(response.get("memory").toString()));
                } catch (NumberFormatException e) {
                    result.setMemoryUsed(0.0);
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