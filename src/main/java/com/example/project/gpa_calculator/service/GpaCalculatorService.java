package com.example.project.gpa_calculator.service;

import com.example.project.gpa_calculator.dto.response.CgpaResponse;
import com.example.project.gpa_calculator.entity.Semester;
import com.example.project.gpa_calculator.entity.Subject;
import com.example.project.gpa_calculator.entity.User;

import java.util.List;

public interface GpaCalculatorService {

    double gradeToPoint(String grade);

    int calculateSemesterCredits(List<Subject> subjects);

    double calculateSemesterGradePoints(List<Subject> subjects);

    void recalculateSemesterStats(Semester semester);

    void updateUserTotals(User user);

    CgpaResponse getCgpa();
}