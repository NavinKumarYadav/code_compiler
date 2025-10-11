package com.compiler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 10000, message = "Code cannot exceed 10000 characters")
    private String code;

    @NotBlank(message = "Language cannot be empty")
    private String language;
    private String input;
    private String expectedOutput;
    private String compilerOptions;
}
