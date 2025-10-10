package com.compiler.service;


import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    @Value("${judge0.api.key}")
    private String rapidApiKey;

    private final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";

    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, Integer> LANGUAGE_IDS = createLanguageMap();

    private Map<String, Integer> createLanguageMap(){
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
        return Collections.unmodifiableMap(languages);
    }

    public ExecutionResponse executeCode(ExecutionRequest request){
        if(!LANGUAGE_IDS.containsKey(request.getLanguage())){
            return ExecutionResponse.error("Unsupported language: " + request.getLanguage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("source_code", request.getCode());
        body.put("language_id", LANGUAGE_IDS.get(request.getLanguage()));
        body.put("stdin", request.getInput() != null ? request.getInput() : "");

        if(request.getExpectedOutput() != null){
            body.put("expected_output", request.getExpectedOutput());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try{
            ResponseEntity<Map> response = restTemplate.exchange(
                    JUDGE0_URL, HttpMethod.POST, entity, Map.class);

            return mapToExecutionResponse(response.getBody(), request.getExpectedOutput());
        }catch(Exception e){
            return ExecutionResponse.error("Execution failed: " + e.getMessage());
        }
    }

    public Map<String, Object> execute(String source, String language){
        Map<String, Object> body = new HashMap<>();
        body.put("source_code", source);
        body.put("language_id", getLanguageId(language));
        body.put("stdin", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(JUDGE0_URL, HttpMethod.POST, entity, Map.class).getBody();
    }

    private ExecutionResponse mapToExecutionResponse(Map<String, Object> response, String expectedOutput){
        ExecutionResponse result = new ExecutionResponse();

        if(response.containsKey("stdout")){
            result.setOutput(response.get("stdout") != null ? response.get("stdout").toString() : "");
        }

        if (response.containsKey("stderr")) {
            result.setError(response.get("stderr") != null ? response.get("stderr").toString() : "");
        }

        if (response.containsKey("time")) {
            result.setExecutionTime(response.get("time") != null ?
                    Double.parseDouble(response.get("time").toString()) : 0.0);
        }
        if (response.containsKey("memory")) {
            result.setMemoryUsed(response.get("memory") != null ?
                    Double.parseDouble(response.get("memory").toString()) : 0.0);
        }
        if (response.containsKey("status")) {
            Map<String, Object> status = (Map<String, Object>) response.get("status");
            result.setStatus(status.get("description").toString());
        }

        if (expectedOutput != null && !expectedOutput.isEmpty()) {
            String actualOutput = result.getOutput() != null ? result.getOutput().trim() : "";
            result.setIsCorrect(actualOutput.equals(expectedOutput.trim()));
        }

        return result;
    }

    private int getLanguageId(String language) {
        return LANGUAGE_IDS.getOrDefault(language.toLowerCase(), 62);
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
