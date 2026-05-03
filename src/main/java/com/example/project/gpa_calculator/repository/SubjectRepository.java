package com.example.project.gpa_calculator.repository;

import com.example.project.gpa_calculator.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject,Long> {
}
