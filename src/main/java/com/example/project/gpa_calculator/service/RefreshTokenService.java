package com.example.project.gpa_calculator.service;

import com.example.project.gpa_calculator.entity.RefreshToken;
import com.example.project.gpa_calculator.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);
}