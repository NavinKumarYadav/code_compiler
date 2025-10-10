package com.compiler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {

    private String code;
    private String language;
    private String input;
    private String expectedOutput;
    private String compilerOptions;
}
