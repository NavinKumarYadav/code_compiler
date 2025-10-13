package com.compiler.controller;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.service.Judge0Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/compile")
@Tag(name = "Code Compiler", description = "APIs for executing code in multiple programming languages")
public class CompilerController {

    private final Judge0Service judge0Service;

    public CompilerController(Judge0Service judge0Service){
        this.judge0Service = judge0Service;
    }

    @Operation(
            summary = "Execute code",
            description = "Compile and execute code in supported programming languages. Supports Java, Python, C++, JavaScript, C#, Ruby, Go, Rust, Kotlin, and Swift."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Code executed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExecutionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - code is empty, language not supported, or validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExecutionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded - too many requests",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExecutionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Judge0 API unavailable or other server issues",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExecutionResponse.class))
            )
    })
    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(
            @Parameter(
                    description = "Code execution request containing code, language, input, and expected output",
                    required = true,
                    schema = @Schema(implementation = ExecutionRequest.class)
            )
            @Valid @RequestBody ExecutionRequest request,
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

    @Operation(
            summary = "Get supported languages",
            description = "Retrieve list of all supported programming languages with their compiler/interpreter versions"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of supported languages retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class))
    )
    @GetMapping("/languages")
    public ResponseEntity<Map<String, String>> getSupportedLanguages() {
        return ResponseEntity.ok(judge0Service.getSupportedLanguages());
    }
}