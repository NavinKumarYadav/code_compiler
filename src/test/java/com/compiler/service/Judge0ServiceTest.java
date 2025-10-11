package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Judge0ServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SubmissionHistoryService submissionHistoryService;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private Judge0Service judge0Service;

    @Test
    void testExecuteCode_RateLimitExceeded() {

        ExecutionRequest request = new ExecutionRequest();
        request.setCode("print('hello')");
        request.setLanguage("python");

        when(rateLimitService.isAllowed(anyString())).thenReturn(false);

        ExecutionResponse response = judge0Service.executeCode(request);

        assertNotNull(response);
        assertTrue(response.getError().contains("Rate limit exceeded"));
    }

    @Test
    void testExecuteCode_EmptyCode() {
        ExecutionRequest request = new ExecutionRequest();
        request.setCode("");
        request.setLanguage("python");

        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        ExecutionResponse response = judge0Service.executeCode(request);

        assertNotNull(response);
        assertTrue(response.getError().contains("Source code cannot be empty"));
    }

    @Test
    void testExecuteCode_UnsupportedLanguage() {
        ExecutionRequest request = new ExecutionRequest();
        request.setCode("print('hello')");
        request.setLanguage("unknown_language");

        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        ExecutionResponse response = judge0Service.executeCode(request);

        assertNotNull(response);
        assertTrue(response.getError().contains("Unsupported language"));
    }

    @Test
    void testExecuteCode_NullCode() {

        ExecutionRequest request = new ExecutionRequest();
        request.setCode(null);
        request.setLanguage("python");

        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        ExecutionResponse response = judge0Service.executeCode(request);

        assertNotNull(response);
        assertTrue(response.getError().contains("Source code cannot be empty"));
    }
}