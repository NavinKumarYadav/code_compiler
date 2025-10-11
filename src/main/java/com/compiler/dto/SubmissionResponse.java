package com.compiler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private String status;
    private Double runtimeMs;
    private Double memoryKb;
    private String language;
    private String output;
    private String error;
    private String submittedAt;
}