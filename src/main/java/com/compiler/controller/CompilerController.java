package com.compiler.controller;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.service.Judge0Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/compile")
public class CompilerController {

    private final Judge0Service judge0Service;

    public CompilerController(Judge0Service judge0Service){
        this.judge0Service = judge0Service;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(@Valid @RequestBody ExecutionRequest request,
                                                         HttpServletRequest httpRequest) {
        try {
            System.out.println("Received execution request for language: " + request.getLanguage());
            ExecutionResponse result = judge0Service.executeCode(request, httpRequest);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            System.out.println("Error in compilation: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ExecutionResponse.error("Compilation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String, String>> getSupportedLanguages() {
        return ResponseEntity.ok(judge0Service.getSupportedLanguages());
    }
}