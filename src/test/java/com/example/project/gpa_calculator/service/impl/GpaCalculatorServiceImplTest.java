package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.dto.response.CgpaResponse;
import com.example.project.gpa_calculator.entity.Semester;
import com.example.project.gpa_calculator.entity.Subject;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.repository.SemesterRepository;
import com.example.project.gpa_calculator.repository.UserRepository;
import com.example.project.gpa_calculator.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpaCalculatorServiceImplTest {

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private GpaCalculatorServiceImpl gpaCalculatorService;

    @Test
    void gradeToPoint_shouldReturnCorrectPoints_forValidGrades() {
        assertThat(gpaCalculatorService.gradeToPoint("A+")).isEqualTo(4.0);
        assertThat(gpaCalculatorService.gradeToPoint("A")).isEqualTo(4.0);
        assertThat(gpaCalculatorService.gradeToPoint("A-")).isEqualTo(3.7);
        assertThat(gpaCalculatorService.gradeToPoint("B+")).isEqualTo(3.3);
        assertThat(gpaCalculatorService.gradeToPoint("B")).isEqualTo(3.0);
        assertThat(gpaCalculatorService.gradeToPoint("B-")).isEqualTo(2.7);
        assertThat(gpaCalculatorService.gradeToPoint("C+")).isEqualTo(2.3);
        assertThat(gpaCalculatorService.gradeToPoint("C")).isEqualTo(2.0);
        assertThat(gpaCalculatorService.gradeToPoint("C-")).isEqualTo(1.7);
        assertThat(gpaCalculatorService.gradeToPoint("D+")).isEqualTo(1.3);
        assertThat(gpaCalculatorService.gradeToPoint("D")).isEqualTo(1.0);
        assertThat(gpaCalculatorService.gradeToPoint("F")).isEqualTo(0.0);
        assertThat(gpaCalculatorService.gradeToPoint("-")).isEqualTo(0.0);
    }

    @Test
    void gradeToPoint_shouldHandleLowercaseAndSpaces() {
        assertThat(gpaCalculatorService.gradeToPoint(" a- ")).isEqualTo(3.7);
        assertThat(gpaCalculatorService.gradeToPoint(" b+ ")).isEqualTo(3.3);
    }

    @Test
    void gradeToPoint_shouldThrowException_whenGradeIsNull() {
        assertThatThrownBy(() -> gpaCalculatorService.gradeToPoint(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Grade cannot be null");
    }

    @Test
    void gradeToPoint_shouldThrowException_whenGradeIsInvalid() {
        assertThatThrownBy(() -> gpaCalculatorService.gradeToPoint("Z"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid grade: Z");
    }

    @Test
    void calculateSemesterCredits_shouldIgnoreSubjectsWithDashGrade() {
        Subject math = new Subject();
        math.setGrade("A");
        math.setCredit(3);

        Subject physics = new Subject();
        physics.setGrade("B+");
        physics.setCredit(3);

        Subject ignored = new Subject();
        ignored.setGrade("-");
        ignored.setCredit(4);

        int credits = gpaCalculatorService.calculateSemesterCredits(
                List.of(math, physics, ignored)
        );

        assertThat(credits).isEqualTo(6);
    }

    @Test
    void calculateSemesterGradePoints_shouldCalculateCorrectTotalPoints() {
        Subject math = new Subject();
        math.setGrade("A");
        math.setCredit(3);

        Subject physics = new Subject();
        physics.setGrade("B+");
        physics.setCredit(3);

        Subject ignored = new Subject();
        ignored.setGrade("-");
        ignored.setCredit(4);

        double totalPoints = gpaCalculatorService.calculateSemesterGradePoints(
                List.of(math, physics, ignored)
        );

        // A = 4.0, B+ = 3.3
        // (4.0 * 3) + (3.3 * 3) = 12 + 9.9 = 21.9
        assertThat(totalPoints).isEqualTo(21.9);
    }

    @Test
    void recalculateSemesterStats_shouldSetCreditsAndGpa() {
        Subject math = new Subject();
        math.setGrade("A");
        math.setCredit(3);

        Subject physics = new Subject();
        physics.setGrade("B+");
        physics.setCredit(3);

        Semester semester = new Semester();
        semester.setSubjects(List.of(math, physics));

        gpaCalculatorService.recalculateSemesterStats(semester);

        assertThat(semester.getSemesterCredits()).isEqualTo(6);
        assertThat(semester.getSemesterGpa()).isEqualTo(3.65);
    }

    @Test
    void recalculateSemesterStats_shouldSetZeroGpa_whenNoValidCredits() {
        Subject subject = new Subject();
        subject.setGrade("-");
        subject.setCredit(3);

        Semester semester = new Semester();
        semester.setSubjects(List.of(subject));

        gpaCalculatorService.recalculateSemesterStats(semester);

        assertThat(semester.getSemesterCredits()).isEqualTo(0);
        assertThat(semester.getSemesterGpa()).isEqualTo(0.0);
    }

    @Test
    void updateUserTotals_shouldCalculateAndSaveUserTotals() {
        User user = new User();
        user.setId(1L);

        Semester semester1 = new Semester();
        semester1.setSemesterGpa(4.0);
        semester1.setSemesterCredits(3);

        Semester semester2 = new Semester();
        semester2.setSemesterGpa(2.0);
        semester2.setSemesterCredits(9);

        when(semesterRepository.findByUserId(1L))
                .thenReturn(List.of(semester1, semester2));

        gpaCalculatorService.updateUserTotals(user);

        // CGPA = (4.0 * 3 + 2.0 * 9) / 12 = 2.5
        assertThat(user.getTotalCredits()).isEqualTo(12);
        assertThat(user.getTotalGpa()).isEqualTo(2.5);

        verify(semesterRepository).findByUserId(1L);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserTotals_shouldSetZeroValues_whenUserHasNoSemesters() {
        User user = new User();
        user.setId(1L);

        when(semesterRepository.findByUserId(1L))
                .thenReturn(List.of());

        gpaCalculatorService.updateUserTotals(user);

        assertThat(user.getTotalCredits()).isEqualTo(0);
        assertThat(user.getTotalGpa()).isEqualTo(0.0);

        verify(semesterRepository).findByUserId(1L);
        verify(userRepository).save(user);
    }

    @Test
    void getCgpa_shouldReturnCgpaResponseForCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setTotalGpa(3.65);
        user.setTotalCredits(6);

        Semester semester1 = new Semester();
        semester1.setId(1L);

        Semester semester2 = new Semester();
        semester2.setId(2L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(semesterRepository.findByUserId(1L))
                .thenReturn(List.of(semester1, semester2));

        CgpaResponse response = gpaCalculatorService.getCgpa();

        assertThat(response.getCgpa()).isEqualTo(3.65);
        assertThat(response.getTotalCredits()).isEqualTo(6);
        assertThat(response.getSemesterCount()).isEqualTo(2);

        verify(currentUserService).getCurrentUser();
        verify(semesterRepository).findByUserId(1L);
    }
}