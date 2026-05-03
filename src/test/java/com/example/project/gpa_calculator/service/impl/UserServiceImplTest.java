package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.dto.response.UserResponse;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.exception.InvalidCredentialsException;
import com.example.project.gpa_calculator.mapper.RegisterMapper;
import com.example.project.gpa_calculator.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegisterMapper registerMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUser_shouldReturnUserResponse_whenUserExists() {
        String email = "mohamed@test.com";

        User user = new User();
        user.setId(1L);
        user.setName("Mohamed");
        user.setEmail(email);

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setName("Mohamed");
        expectedResponse.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(registerMapper.userToUserResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.getCurrentUser(email);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getEmail()).isEqualTo(email);

        verify(userRepository).findByEmail(email);
        verify(registerMapper).userToUserResponse(user);
    }

    @Test
    void getCurrentUser_shouldThrowException_whenUserDoesNotExist() {
        String email = "missing@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(email))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(email);
    }
}