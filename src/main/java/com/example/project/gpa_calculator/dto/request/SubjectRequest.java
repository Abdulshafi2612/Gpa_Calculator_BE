package com.example.project.gpa_calculator.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String grade;

    @NotNull
    private int credit;

    @Min(1)
    private Integer sequence;
}
