package com.example.project.gpa_calculator.repository;

import com.example.project.gpa_calculator.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository  extends JpaRepository<Semester,Long> {

    Optional<Semester> findByIdAndUserId(Long id, Long userId);

    List<Semester> findByUserId(Long userId);

    boolean  existsByUserIdAndSequence(Long id, Integer sequence);

    @Query("select max(s.sequence) from Semester s where s.user.id = :userId")
    Integer findMaxSequenceByUserId(@Param("userId") Long userId);}
