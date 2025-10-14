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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/compile")
@RequiredArgsConstructor
@Tag(name = "Code Compiler", description = "APIs for executing code in multiple programming languages")
public class CompilerController {

    private final Judge0Service judge0Service;


    @Operation(
            summary = "Execute code",
            description = "Compile and execute code in supported programming languages. Supports Java, " +
                    "Python, C++, JavaScript, C#, Ruby, Go, Rust, Kotlin, and Swift."
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
            log.info("Received execution request for language: {}", request.getLanguage());

            if (request == null) {
                log.warn("Null execution request received");
                return ResponseEntity.badRequest()
                        .body(ExecutionResponse.error("Request cannot be null"));
            }

            ExecutionResponse result = judge0Service.executeCode(request, httpRequest);
            log.info("Code execution completed successfully for language: {}", request.getLanguage());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error executing code for language {}: {}",
                    request != null ? request.getLanguage() : "unknown",
                    e.getMessage(), e);

            String errorMessage = "Compilation failed: " + e.getMessage();
            if (e.getMessage().contains("Security violation") ||
                    e.getMessage().contains("Rate limit") ||
                    e.getMessage().contains("Unsupported language")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ExecutionResponse.error(errorMessage));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ExecutionResponse.error(errorMessage));
            }
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
        log.debug("Fetching supported languages");
        try {
            Map<String, String> languages = judge0Service.getSupportedLanguages();
            log.info("Returning {} supported languages", languages.size());
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            log.error("Error fetching supported languages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch supported languages"));
        }
    }

    @Operation(
            summary = "Health check",
            description = "Check if the compiler service is running"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(mediaType = "application/json")
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.debug("Health check endpoint called");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "code-compiler",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @Operation(
            summary = "Get service status",
            description = "Get current service status and information"
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = Map.of(
                "service", "Code Compiler API",
                "version", "1.0.0",
                "status", "operational",
                "supportedLanguages", judge0Service.getSupportedLanguages().size(),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(status);
    }
}