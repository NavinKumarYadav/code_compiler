package com.compiler.service;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";

    public Map<String, Object> execute(String source, String language){
        Map<String, String> payload = new HashMap<>();
        payload.put("source_code", source);
        payload.put("language_id", language);
        payload.put("stdin", "");
        return restTemplate.postForObject(JUDGE0_URL, payload, Map.class);
    }

}
