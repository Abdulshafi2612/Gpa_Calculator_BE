package com.example.project.gpa_calculator.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SemesterRequest {

    @Min(1)
    private Integer sequence;

    @Valid
    @NotEmpty
    private List<SubjectRequest> subjects;

}
