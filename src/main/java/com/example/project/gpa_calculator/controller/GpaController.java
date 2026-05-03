package com.example.project.gpa_calculator.controller;

import com.example.project.gpa_calculator.dto.response.CgpaResponse;
import com.example.project.gpa_calculator.service.GpaCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GpaController {

    private final GpaCalculatorService gpaCalculatorService;

    @GetMapping("/api/cgpa")
    public ResponseEntity<CgpaResponse> getCgpa() {
        CgpaResponse response = gpaCalculatorService.getCgpa();
        return ResponseEntity.ok(response);
    }
}