package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.dto.response.UserResponse;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.exception.InvalidCredentialsException;
import com.example.project.gpa_calculator.mapper.RegisterMapper;
import com.example.project.gpa_calculator.repository.UserRepository;
import com.example.project.gpa_calculator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return registerMapper.userToUserResponse(user);
    }

}