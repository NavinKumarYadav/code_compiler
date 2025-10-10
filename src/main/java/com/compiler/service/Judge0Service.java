package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    @Value("${judge0.api.key}")
    private String rapidApiKey;

    private final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Integer> LANGUAGE_IDS = createLanguageMap();

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
        System.out.println("=== JUDGE0 DEBUG ===");
        System.out.println("Language: " + request.getLanguage());
        System.out.println("Code length: " + (request.getCode() != null ? request.getCode().length() : "NULL"));
        System.out.println("Language ID: " + LANGUAGE_IDS.get(request.getLanguage()));

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ExecutionResponse.error("Source code cannot be empty");
        }

        if (request.getLanguage() == null || !LANGUAGE_IDS.containsKey(request.getLanguage())) {
            return ExecutionResponse.error("Unsupported language: " + request.getLanguage());
        }

        Judge0Submission submission = new Judge0Submission();
        submission.source_code = request.getCode();
        submission.language_id = LANGUAGE_IDS.get(request.getLanguage());
        submission.stdin = request.getInput() != null ? request.getInput() : "";
        submission.expected_output = request.getExpectedOutput();

        System.out.println("Submission: " + submission);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

        HttpEntity<Judge0Submission> entity = new HttpEntity<>(submission, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    JUDGE0_URL, HttpMethod.POST, entity, Map.class);

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            return mapToExecutionResponse(response.getBody(), request.getExpectedOutput());

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ExecutionResponse.error("Execution failed: " + e.getMessage());
        }
    }

    private static class Judge0Submission {
        public String source_code;
        public int language_id;
        public String stdin;
        public String expected_output;

        @Override
        public String toString() {
            return "Judge0Submission{" +
                    "source_code='" + (source_code != null ? source_code.substring(0, Math.min(30, source_code.length())) + "..." : "null") + '\'' +
                    ", language_id=" + language_id +
                    ", stdin='" + stdin + '\'' +
                    ", expected_output='" + expected_output + '\'' +
                    '}';
        }
    }

    private ExecutionResponse mapToExecutionResponse(Map<String, Object> response, String expectedOutput) {
        ExecutionResponse result = new ExecutionResponse();

        if (response != null) {
            if (response.containsKey("stdout") && response.get("stdout") != null) {
                result.setOutput(response.get("stdout").toString().trim());
            }

            if (response.containsKey("stderr") && response.get("stderr") != null) {
                result.setError(response.get("stderr").toString().trim());
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
                Map<String, Object> status = (Map<String, Object>) response.get("status");
                if (status != null && status.containsKey("description")) {
                    result.setStatus(status.get("description").toString());
                }
            }

            if (expectedOutput != null && !expectedOutput.isEmpty()) {
                String actualOutput = result.getOutput() != null ? result.getOutput().trim() : "";
                result.setIsCorrect(actualOutput.equals(expectedOutput.trim()));
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