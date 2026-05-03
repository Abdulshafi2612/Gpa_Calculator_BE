package com.example.project.gpa_calculator.mapper;

import com.example.project.gpa_calculator.dto.request.SubjectRequest;
import com.example.project.gpa_calculator.dto.response.SubjectResponse;
import com.example.project.gpa_calculator.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "semester", ignore = true)
    Subject subjectRequestToSubject(SubjectRequest request);

    SubjectResponse subjectToSubjectResponse(Subject subject);
}
