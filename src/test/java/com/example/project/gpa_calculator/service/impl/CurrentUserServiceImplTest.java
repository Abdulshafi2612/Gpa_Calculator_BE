package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.repository.UserRepository;
import com.example.project.gpa_calculator.service.CurrentUserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserServiceImpl currentUserService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticatedUserExists() {
        String email = "mohamed@test.com";

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setName("Mohamed");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = currentUserService.getCurrentUser();

        assertThat(result).isEqualTo(user);
        assertThat(result.getEmail()).isEqualTo(email);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUser_shouldThrowException_whenAuthenticatedUserNotFound() {
        String email = "missing@test.com";

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Current user not found");

        verify(userRepository).findByEmail(email);
    }
}