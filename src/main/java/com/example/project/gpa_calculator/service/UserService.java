package com.example.project.gpa_calculator.service;

import com.example.project.gpa_calculator.dto.response.CgpaResponse;
import com.example.project.gpa_calculator.dto.response.UserResponse;
import com.example.project.gpa_calculator.entity.User;

public interface UserService {

    UserResponse getCurrentUser(String email);


}