package com.example.project.gpa_calculator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CgpaResponse {

    private double cgpa;

    private int totalCredits;

    private int semesterCount;
}
