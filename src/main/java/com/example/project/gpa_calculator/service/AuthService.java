package com.example.project.gpa_calculator.service;

import com.example.project.gpa_calculator.dto.request.LoginRequest;
import com.example.project.gpa_calculator.dto.request.RefreshTokenRequest;
import com.example.project.gpa_calculator.dto.request.RegisterRequest;
import com.example.project.gpa_calculator.dto.response.AuthResponse;
import com.example.project.gpa_calculator.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}