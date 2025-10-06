package com.compiler.dto;

import com.compiler.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long submissionId;
    private SubmissionStatus status;
    private Integer runtimeMs;
    private Integer memoryKb;
}
