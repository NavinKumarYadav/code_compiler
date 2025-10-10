package com.compiler.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test-protected")
    public ResponseEntity<Map<String,String>> testProtected(){
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Protected endpoint is working!");
        response.put("status", "JWT token is valid");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submissions")
    public ResponseEntity<Map<String, Object>> getSubmissions(){
        Map<String, Object> response = new HashMap<>();
        response.put("username", "test@test.com");
        response.put("role", "USER");
        response.put("message", "User profile endpoint");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getUserProfile() {
        Map<String, String> response = new HashMap<>();
        response.put("username", "test@test.com");
        response.put("role", "USER");
        response.put("message", "User profile endpoint");
        return ResponseEntity.ok(response);
    }
}
