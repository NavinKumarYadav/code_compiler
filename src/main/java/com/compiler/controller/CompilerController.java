package com.compiler.controller;


import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.service.Judge0Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/compile")
public class CompilerController {

    private final Judge0Service judge0Service;

    public CompilerController(Judge0Service judge0Service){
        this.judge0Service=judge0Service;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(@RequestBody ExecutionRequest request){
        try {
            ExecutionResponse result = judge0Service.executeCode(request);
            return ResponseEntity.ok(result);
        }catch(Exception e){
            return ResponseEntity.badRequest()
                    .body(ExecutionResponse.error("Compilation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String,String>> getSupportedLanguages(){
        return ResponseEntity.ok(judge0Service.getSupportedLanguages());
    }
}
