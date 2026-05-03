package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.dto.request.SemesterRequest;
import com.example.project.gpa_calculator.dto.response.AllSemestersResponse;
import com.example.project.gpa_calculator.dto.response.SemesterResponse;
import com.example.project.gpa_calculator.entity.Semester;
import com.example.project.gpa_calculator.entity.Subject;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.exception.DuplicateSemesterSequenceException;
import com.example.project.gpa_calculator.mapper.SemesterMapper;
import com.example.project.gpa_calculator.mapper.SubjectMapper;
import com.example.project.gpa_calculator.repository.SemesterRepository;
import com.example.project.gpa_calculator.service.CurrentUserService;
import com.example.project.gpa_calculator.service.GpaCalculatorService;
import com.example.project.gpa_calculator.service.SemesterService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;
    private final SemesterMapper semesterMapper;
    private final CurrentUserService currentUserService;
    private final SubjectMapper subjectMapper;
    private final GpaCalculatorService gpaCalculatorService;


    @Override
    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {

        User user = currentUserService.getCurrentUser();
        Semester semester = semesterMapper.semesterRequestToSemester(request);

        semester.setUser(user);

        for (Subject subject : semester.getSubjects()) {
            subject.setSemester(semester);
        }

        gpaCalculatorService.recalculateSemesterStats(semester);

        Integer requestedSequence = request.getSequence();

        int sequence;

        if (requestedSequence != null) {
            sequence = requestedSequence;

            if (semesterRepository.existsByUserIdAndSequence(user.getId(), sequence)) {
                throw new DuplicateSemesterSequenceException("Semester sequence already exists");
            }
        } else {
            Integer maxSequence = semesterRepository.findMaxSequenceByUserId(user.getId());
            sequence = maxSequence == null ? 1 : maxSequence + 1;
        }

        semester.setSequence(sequence);

        Semester savedSemester = semesterRepository.save(semester);

        gpaCalculatorService.updateUserTotals(user);
        return semesterMapper.semesterToSemesterResponse(savedSemester);
    }

    @Override
    public List<AllSemestersResponse> getAllSemesters() {
        User user = currentUserService.getCurrentUser();
        List<Semester> semesters = semesterRepository.findByUserId(user.getId());

        return semesterMapper.semestersToAllSemestersResponse(semesters);
    }


    @Override
    public SemesterResponse getSemesterById(Long id) {
        User user = currentUserService.getCurrentUser();
        Semester semester = semesterRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found"));

        return semesterMapper.semesterToSemesterResponse(semester);

    }

    @Transactional
    @Override
    public void deleteSemesterById(Long id) {
        User user = currentUserService.getCurrentUser();

        Semester semester = semesterRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found"));

        semesterRepository.delete(semester);
        semesterRepository.flush();


        gpaCalculatorService.updateUserTotals(user);
    }

    @Transactional
    @Override
    public SemesterResponse updateSemester(Long id, SemesterRequest request) {
        User user = currentUserService.getCurrentUser();

        Semester semester = semesterRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found"));

        semester.getSubjects().clear();

        List<Subject> newSubjects = request.getSubjects()
                .stream()
                .map(subjectMapper::subjectRequestToSubject)
                .toList();

        for (Subject subject : newSubjects) {
            subject.setSemester(semester);
            semester.getSubjects().add(subject);
        }

        gpaCalculatorService.recalculateSemesterStats(semester);

        Semester savedSemester = semesterRepository.save(semester);
        gpaCalculatorService.updateUserTotals(user);
        return semesterMapper.semesterToSemesterResponse(savedSemester);
    }


}