package com.example.project.gpa_calculator.mapper;

import com.example.project.gpa_calculator.dto.request.RegisterRequest;
import com.example.project.gpa_calculator.dto.response.UserResponse;
import com.example.project.gpa_calculator.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "totalGpa", ignore = true)
    @Mapping(target = "totalCredits", ignore = true)
    @Mapping(target = "semesters", ignore = true)
    User registerToUser(RegisterRequest registerRequest, @Context String passwordHash);

    @AfterMapping
    default void setPasswordHash(
            @MappingTarget User user,
            @Context String passwordHash
    ) {
        user.setPasswordHash(passwordHash);
    }

    UserResponse userToUserResponse(User user);
}