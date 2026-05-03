package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.dto.request.LoginRequest;
import com.example.project.gpa_calculator.dto.request.RefreshTokenRequest;
import com.example.project.gpa_calculator.dto.request.RegisterRequest;
import com.example.project.gpa_calculator.dto.response.AuthResponse;
import com.example.project.gpa_calculator.dto.response.UserResponse;
import com.example.project.gpa_calculator.entity.RefreshToken;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.exception.EmailAlreadyExistsException;
import com.example.project.gpa_calculator.exception.InvalidCredentialsException;
import com.example.project.gpa_calculator.mapper.RegisterMapper;
import com.example.project.gpa_calculator.repository.UserRepository;
import com.example.project.gpa_calculator.security.JwtService;
import com.example.project.gpa_calculator.service.AuthService;
import com.example.project.gpa_calculator.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterMapper registerMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String rawPassword = request.getPassword();
        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = registerMapper.registerToUser(request, passwordHash);

        User savedUser = userRepository.save(user);
        return registerMapper.userToUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(email);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(
                request.getRefreshToken()
        );

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                newAccessToken,
                refreshToken.getToken()
        );
    }
}