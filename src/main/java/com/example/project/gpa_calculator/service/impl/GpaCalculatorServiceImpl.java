package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.dto.response.CgpaResponse;
import com.example.project.gpa_calculator.entity.Semester;
import com.example.project.gpa_calculator.entity.Subject;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.repository.SemesterRepository;
import com.example.project.gpa_calculator.repository.UserRepository;
import com.example.project.gpa_calculator.service.CurrentUserService;
import com.example.project.gpa_calculator.service.GpaCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GpaCalculatorServiceImpl implements GpaCalculatorService {

    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Override
    public double gradeToPoint(String grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }

        return switch (grade.trim().toUpperCase()) {
            case "A+", "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "F", "-" -> 0.0;
            default -> throw new IllegalArgumentException("Invalid grade: " + grade);
        };
    }

    @Override
    public int calculateSemesterCredits(List<Subject> subjects) {
        return subjects.stream()
                .filter(subject -> !"-".equals(subject.getGrade()))
                .mapToInt(Subject::getCredit)
                .sum();
    }

    @Override
    public double calculateSemesterGradePoints(List<Subject> subjects) {
        return subjects.stream()
                .filter(subject -> !"-".equals(subject.getGrade()))
                .mapToDouble(subject ->
                        gradeToPoint(subject.getGrade()) * subject.getCredit()
                )
                .sum();
    }

    @Override
    public void recalculateSemesterStats(Semester semester) {
        int semesterCredits = calculateSemesterCredits(semester.getSubjects());
        semester.setSemesterCredits(semesterCredits);

        double semesterGradePoints = calculateSemesterGradePoints(semester.getSubjects());

        double semesterGpa = semesterCredits == 0
                ? 0.0
                : semesterGradePoints / semesterCredits;

        semester.setSemesterGpa(semesterGpa);
    }

    @Override
    public void updateUserTotals(User user) {
        List<Semester> semesters = semesterRepository.findByUserId(user.getId());

        int totalCredits = semesters.stream()
                .mapToInt(Semester::getSemesterCredits)
                .sum();

        double totalGradePoints = semesters.stream()
                .mapToDouble(semester ->
                        semester.getSemesterGpa() * semester.getSemesterCredits()
                )
                .sum();

        double cgpa = totalCredits == 0
                ? 0.0
                : totalGradePoints / totalCredits;

        user.setTotalCredits(totalCredits);
        user.setTotalGpa(cgpa);

        userRepository.save(user);
    }

    @Override
    public CgpaResponse getCgpa() {
        User user = currentUserService.getCurrentUser();

        int semesterCount = semesterRepository.findByUserId(user.getId()).size();

        return new CgpaResponse(
                user.getTotalGpa(),
                user.getTotalCredits(),
                semesterCount
        );
    }
}