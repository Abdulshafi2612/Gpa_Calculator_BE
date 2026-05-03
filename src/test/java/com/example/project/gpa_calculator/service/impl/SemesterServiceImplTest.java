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
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterServiceImplTest {

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private SemesterMapper semesterMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private GpaCalculatorService gpaCalculatorService;

    @InjectMocks
    private SemesterServiceImpl semesterService;

    @Test
    void createSemester_shouldCreateSemesterWithRequestedSequence() {
        User user = new User();
        user.setId(1L);

        SemesterRequest request = new SemesterRequest();
        request.setSequence(2);

        Subject subject = new Subject();
        subject.setName("Math");

        Semester semester = new Semester();
        semester.setSubjects(new ArrayList<>(List.of(subject)));

        Semester savedSemester = new Semester();
        savedSemester.setId(10L);
        savedSemester.setSequence(2);
        savedSemester.setUser(user);
        savedSemester.setSubjects(new ArrayList<>(List.of(subject)));

        SemesterResponse expectedResponse = new SemesterResponse();
        expectedResponse.setId(10L);
        expectedResponse.setSequence(2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterMapper.semesterRequestToSemester(request)).thenReturn(semester);
        when(semesterRepository.existsByUserIdAndSequence(1L, 2)).thenReturn(false);
        when(semesterRepository.save(semester)).thenReturn(savedSemester);
        when(semesterMapper.semesterToSemesterResponse(savedSemester)).thenReturn(expectedResponse);

        SemesterResponse result = semesterService.createSemester(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(semester.getUser()).isEqualTo(user);
        assertThat(semester.getSequence()).isEqualTo(2);
        assertThat(subject.getSemester()).isEqualTo(semester);

        verify(gpaCalculatorService).recalculateSemesterStats(semester);
        verify(gpaCalculatorService).updateUserTotals(user);
        verify(semesterRepository).save(semester);
    }

    @Test
    void createSemester_shouldAutoGenerateSequence_whenSequenceIsNull() {
        User user = new User();
        user.setId(1L);

        SemesterRequest request = new SemesterRequest();
        request.setSequence(null);

        Subject subject = new Subject();

        Semester semester = new Semester();
        semester.setSubjects(new ArrayList<>(List.of(subject)));

        Semester savedSemester = new Semester();
        savedSemester.setId(10L);
        savedSemester.setSequence(4);

        SemesterResponse expectedResponse = new SemesterResponse();
        expectedResponse.setId(10L);
        expectedResponse.setSequence(4);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterMapper.semesterRequestToSemester(request)).thenReturn(semester);
        when(semesterRepository.findMaxSequenceByUserId(1L)).thenReturn(3);
        when(semesterRepository.save(semester)).thenReturn(savedSemester);
        when(semesterMapper.semesterToSemesterResponse(savedSemester)).thenReturn(expectedResponse);

        SemesterResponse result = semesterService.createSemester(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(semester.getSequence()).isEqualTo(4);

        verify(semesterRepository).findMaxSequenceByUserId(1L);
        verify(gpaCalculatorService).recalculateSemesterStats(semester);
        verify(gpaCalculatorService).updateUserTotals(user);
    }

    @Test
    void createSemester_shouldUseSequenceOne_whenUserHasNoSemestersAndSequenceIsNull() {
        User user = new User();
        user.setId(1L);

        SemesterRequest request = new SemesterRequest();
        request.setSequence(null);

        Semester semester = new Semester();
        semester.setSubjects(new ArrayList<>());

        Semester savedSemester = new Semester();
        savedSemester.setId(10L);
        savedSemester.setSequence(1);

        SemesterResponse expectedResponse = new SemesterResponse();
        expectedResponse.setId(10L);
        expectedResponse.setSequence(1);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterMapper.semesterRequestToSemester(request)).thenReturn(semester);
        when(semesterRepository.findMaxSequenceByUserId(1L)).thenReturn(null);
        when(semesterRepository.save(semester)).thenReturn(savedSemester);
        when(semesterMapper.semesterToSemesterResponse(savedSemester)).thenReturn(expectedResponse);

        SemesterResponse result = semesterService.createSemester(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(semester.getSequence()).isEqualTo(1);
    }

    @Test
    void createSemester_shouldThrowException_whenRequestedSequenceAlreadyExists() {
        User user = new User();
        user.setId(1L);

        SemesterRequest request = new SemesterRequest();
        request.setSequence(1);

        Semester semester = new Semester();
        semester.setSubjects(new ArrayList<>());

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterMapper.semesterRequestToSemester(request)).thenReturn(semester);
        when(semesterRepository.existsByUserIdAndSequence(1L, 1)).thenReturn(true);

        assertThatThrownBy(() -> semesterService.createSemester(request))
                .isInstanceOf(DuplicateSemesterSequenceException.class)
                .hasMessage("Semester sequence already exists");

        verify(semesterRepository, never()).save(any());
        verify(gpaCalculatorService).recalculateSemesterStats(semester);
        verify(gpaCalculatorService, never()).updateUserTotals(any());
    }

    @Test
    void getAllSemesters_shouldReturnCurrentUserSemesters() {
        User user = new User();
        user.setId(1L);

        Semester semester1 = new Semester();
        semester1.setId(1L);

        Semester semester2 = new Semester();
        semester2.setId(2L);

        List<Semester> semesters = List.of(semester1, semester2);

        AllSemestersResponse response1 = new AllSemestersResponse();
        response1.setId(1L);

        AllSemestersResponse response2 = new AllSemestersResponse();
        response2.setId(2L);

        List<AllSemestersResponse> expectedResponses = List.of(response1, response2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByUserId(1L)).thenReturn(semesters);
        when(semesterMapper.semestersToAllSemestersResponse(semesters)).thenReturn(expectedResponses);

        List<AllSemestersResponse> result = semesterService.getAllSemesters();

        assertThat(result).isEqualTo(expectedResponses);

        verify(currentUserService).getCurrentUser();
        verify(semesterRepository).findByUserId(1L);
        verify(semesterMapper).semestersToAllSemestersResponse(semesters);
    }

    @Test
    void getSemesterById_shouldReturnSemester_whenOwnedByCurrentUser() {
        User user = new User();
        user.setId(1L);

        Semester semester = new Semester();
        semester.setId(10L);

        SemesterResponse expectedResponse = new SemesterResponse();
        expectedResponse.setId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(semester));
        when(semesterMapper.semesterToSemesterResponse(semester)).thenReturn(expectedResponse);

        SemesterResponse result = semesterService.getSemesterById(10L);

        assertThat(result).isEqualTo(expectedResponse);

        verify(semesterRepository).findByIdAndUserId(10L, 1L);
        verify(semesterMapper).semesterToSemesterResponse(semester);
    }

    @Test
    void getSemesterById_shouldThrowException_whenSemesterNotFoundOrNotOwned() {
        User user = new User();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> semesterService.getSemesterById(10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Semester not found");

        verify(semesterMapper, never()).semesterToSemesterResponse(any());
    }

    @Test
    void updateSemester_shouldReplaceSubjectsAndRecalculateStats() {
        User user = new User();
        user.setId(1L);

        SemesterRequest request = new SemesterRequest();

        Semester semester = new Semester();
        semester.setId(10L);
        semester.setSubjects(new ArrayList<>(List.of(new Subject())));

        Subject newSubject = new Subject();
        newSubject.setName("Algorithms");

        request.setSubjects(List.of());

        Semester savedSemester = new Semester();
        savedSemester.setId(10L);
        savedSemester.setSubjects(new ArrayList<>(List.of(newSubject)));

        SemesterResponse expectedResponse = new SemesterResponse();
        expectedResponse.setId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(semester));
        when(subjectMapper.subjectRequestToSubject(any())).thenReturn(newSubject);
        when(semesterRepository.save(semester)).thenReturn(savedSemester);
        when(semesterMapper.semesterToSemesterResponse(savedSemester)).thenReturn(expectedResponse);

        // عشان request.getSubjects() مايبقاش null ويدخل stream
        request.setSubjects(List.of(mock(com.example.project.gpa_calculator.dto.request.SubjectRequest.class)));

        SemesterResponse result = semesterService.updateSemester(10L, request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(semester.getSubjects()).containsExactly(newSubject);
        assertThat(newSubject.getSemester()).isEqualTo(semester);

        verify(gpaCalculatorService).recalculateSemesterStats(semester);
        verify(gpaCalculatorService).updateUserTotals(user);
        verify(semesterRepository).save(semester);
    }

    @Test
    void updateSemester_shouldThrowException_whenSemesterNotFoundOrNotOwned() {
        User user = new User();
        user.setId(1L);

        SemesterRequest request = new SemesterRequest();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> semesterService.updateSemester(10L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Semester not found");

        verify(semesterRepository, never()).save(any());
        verify(gpaCalculatorService, never()).updateUserTotals(any());
    }

    @Test
    void deleteSemesterById_shouldDeleteSemesterAndUpdateTotals() {
        User user = new User();
        user.setId(1L);

        Semester semester = new Semester();
        semester.setId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(semester));

        semesterService.deleteSemesterById(10L);

        verify(semesterRepository).delete(semester);
        verify(semesterRepository).flush();
        verify(gpaCalculatorService).updateUserTotals(user);
    }

    @Test
    void deleteSemesterById_shouldThrowException_whenSemesterNotFoundOrNotOwned() {
        User user = new User();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> semesterService.deleteSemesterById(10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Semester not found");

        verify(semesterRepository, never()).delete(any());
        verify(gpaCalculatorService, never()).updateUserTotals(any());
    }
}