package com.example.project.gpa_calculator.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllSemestersResponse {

    private Long id;


    private int sequence;


    private double semesterGpa;


    private int semesterCredits;


}

