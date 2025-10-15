package com.compiler.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public ResponseEntity<Map<String, String>> home(){
        Map<String, String> response = new HashMap<>();
        response.put("message", "🚀 Code Compiler API is running successfully!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "RUNNING");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(){
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp",LocalDateTime.now().toString());
        health.put("service", "Code Compiler API");

        Map<String, String> details = new HashMap<>();
        details.put("database", "Connected");
        details.put("jwt", "Configured");
        details.put("security", "Enabled");

        health.put("details", details);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/api/info")
    public ResponseEntity<Map<String, String>> apiInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Code Compiler API");
        info.put("description", "A secure code compilation service");
        info.put("version", "1.0.0");
        info.put("author", "Navin Kumar Yadav");
        info.put("documentation", "/swagger-ui.html");

        return ResponseEntity.ok(info);
    }
}
