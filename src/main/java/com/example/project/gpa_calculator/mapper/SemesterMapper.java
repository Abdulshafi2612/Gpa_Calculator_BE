package com.example.project.gpa_calculator.mapper;

import com.example.project.gpa_calculator.dto.request.SemesterRequest;
import com.example.project.gpa_calculator.dto.response.AllSemestersResponse;
import com.example.project.gpa_calculator.dto.response.SemesterResponse;
import com.example.project.gpa_calculator.entity.Semester;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = SubjectMapper.class)
public interface SemesterMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "semesterGpa", ignore = true)
    @Mapping(target = "semesterCredits", ignore = true)
    @Mapping(target = "user", ignore = true)
    Semester semesterRequestToSemester(SemesterRequest request);


    SemesterResponse semesterToSemesterResponse(Semester semester);

    List<AllSemestersResponse> semestersToAllSemestersResponse(List<Semester> semesters);
}
