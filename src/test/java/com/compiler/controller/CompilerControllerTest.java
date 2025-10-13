package com.compiler.controller;

import com.compiler.dto.ExecutionRequest;
import com.compiler.dto.ExecutionResponse;
import com.compiler.service.Judge0Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilerController.class)
class CompilerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Judge0Service judge0Service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testExecuteCode_Success() throws Exception {

        ExecutionRequest request = new ExecutionRequest();
        request.setCode("print('hello')");
        request.setLanguage("python");

        ExecutionResponse response = new ExecutionResponse();
        response.setOutput("hello");
        response.setStatus("Accepted");

        when(judge0Service.executeCode(any(ExecutionRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/compile/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value("hello"))
                .andExpect(jsonPath("$.status").value("Accepted"));
    }

    @Test
    void testGetSupportedLanguages() throws Exception {
        Map<String, String> languages = Map.of(
                "java", "Java (OpenJDK 13.0.1)",
                "python", "Python (3.8.1)"
        );
        when(judge0Service.getSupportedLanguages()).thenReturn(languages);

        mockMvc.perform(get("/api/compile/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.java").value("Java (OpenJDK 13.0.1)"))
                .andExpect(jsonPath("$.python").value("Python (3.8.1)"));
    }
}