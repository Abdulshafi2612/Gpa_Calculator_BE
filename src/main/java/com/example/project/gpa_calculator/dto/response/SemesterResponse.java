package com.example.project.gpa_calculator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResponse {

    private Long id;


    private int sequence;


    private double semesterGpa;


    private int semesterCredits;


    private List<SubjectResponse> subjects;
}
