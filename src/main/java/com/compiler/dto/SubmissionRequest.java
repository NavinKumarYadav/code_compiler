package com.compiler.dto;

import lombok.Data;

@Data
public class SubmissionRequest {
    private Long problemId;
    private String language;
    private String code;
}
