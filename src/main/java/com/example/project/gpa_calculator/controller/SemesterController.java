package com.example.project.gpa_calculator.controller;

import com.example.project.gpa_calculator.dto.request.SemesterRequest;
import com.example.project.gpa_calculator.dto.response.AllSemestersResponse;
import com.example.project.gpa_calculator.dto.response.SemesterResponse;
import com.example.project.gpa_calculator.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/semesters")
public class SemesterController {

    private  final SemesterService semesterService;

    @PostMapping
    public ResponseEntity<SemesterResponse> createSemester(@Valid @RequestBody SemesterRequest request) {
        SemesterResponse semesterResponse = semesterService.createSemester(request);
        return new ResponseEntity<>(semesterResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AllSemestersResponse>> getAllSemesters() {
        List<AllSemestersResponse> semestersResponses = semesterService.getAllSemesters();
        return ResponseEntity.ok(semestersResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SemesterResponse> getSemesterById(@PathVariable Long id) {
        SemesterResponse semesterResponse = semesterService.getSemesterById(id);
        return ResponseEntity.ok(semesterResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSemesterById(@PathVariable Long id) {
        semesterService.deleteSemesterById(id);
        return  ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SemesterResponse> updateSemesterById(@PathVariable Long id, @Valid @RequestBody SemesterRequest request) {
        SemesterResponse semesterResponse = semesterService.updateSemester(id, request);
        return ResponseEntity.ok(semesterResponse);
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<SemesterResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(semesterService.toggleActive(id));
    }

}
