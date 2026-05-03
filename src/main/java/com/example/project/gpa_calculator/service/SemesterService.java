package com.example.project.gpa_calculator.service;

import com.example.project.gpa_calculator.dto.request.SemesterRequest;
import com.example.project.gpa_calculator.dto.response.AllSemestersResponse;
import com.example.project.gpa_calculator.dto.response.SemesterResponse;

import java.util.List;
import java.util.Map;

public interface SemesterService {

    SemesterResponse createSemester(SemesterRequest request);

    List<AllSemestersResponse> getAllSemesters();

    SemesterResponse getSemesterById(Long id);

    void deleteSemesterById(Long id);

    SemesterResponse updateSemester(Long id, SemesterRequest request);

    SemesterResponse toggleActive(Long id);

}