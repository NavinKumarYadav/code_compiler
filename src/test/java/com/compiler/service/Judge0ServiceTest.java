package com.compiler.service;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CodeFormatService codeFormatService;

    @Mock
    private HttpServletRequest httpServletRequest; // Add this mock

    @InjectMocks
    private Judge0Service judge0Service;

    private ExecutionRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ExecutionRequest();
        validRequest.setCode("print('hello')");
        validRequest.setLanguage("python");
        validRequest.setInput("");


        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");


        lenient().when(rateLimitService.isAllowed(anyString())).thenReturn(true);
    }

    @Test
    void testExecuteCode_RateLimitExceeded() {

        when(rateLimitService.isAllowed(anyString())).thenReturn(false);

        ExecutionResponse response = judge0Service.executeCode(validRequest, httpServletRequest);

        assertNotNull(response);
        assertTrue(response.getError().contains("Rate limit exceeded"));
        verify(rateLimitService).isAllowed(anyString());
    }

    @Test
    void testExecuteCode_EmptyCode() {
        validRequest.setCode("");

        ExecutionResponse response = judge0Service.executeCode(validRequest, httpServletRequest);

        assertNotNull(response);
        assertTrue(response.getError().contains("Source code cannot be empty"));
    }

    @Test
    void testExecuteCode_UnsupportedLanguage() {
        validRequest.setLanguage("unknown");

        ExecutionResponse response = judge0Service.executeCode(validRequest, httpServletRequest);

        assertNotNull(response);
        assertTrue(response.getError().contains("Unsupported language"));
    }

    @Test
    void testExecuteCode_CodeFormatting() {
        when(codeFormatService.formatCode(anyString(), anyString())).thenReturn("formatted_code");

        judge0Service.executeCode(validRequest, httpServletRequest);

        verify(codeFormatService).formatCode("print('hello')", "python");
    }

    @Test
    void testExecuteCode_SuccessfulExecution() {

        when(codeFormatService.formatCode(anyString(), anyString())).thenReturn("print('hello')");

        ExecutionResponse response = judge0Service.executeCode(validRequest, httpServletRequest);

        assertNotNull(response);

    }

    @Test
    void testExecuteCode_WithUserAuthentication() {

        when(jwtUtil.extractUsername(anyString())).thenReturn("user123");
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");

        ExecutionResponse response = judge0Service.executeCode(validRequest, httpServletRequest);

        assertNotNull(response);
    }
}