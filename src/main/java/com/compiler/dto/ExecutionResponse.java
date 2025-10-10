package com.compiler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {

    private String output;
    private String error;
    private String status;
    private Double executionTime;
    private Double memoryUsed;
    private Boolean isCorrect;

    public static ExecutionResponse error(String message){
        ExecutionResponse response = new ExecutionResponse();
        response.setError(message);
        response.setStatus("Error");
        return response;
    }
}
