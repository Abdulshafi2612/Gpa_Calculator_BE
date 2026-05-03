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
import com.example.project.gpa_calculator.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterMapper registerMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldCreateUserSuccessfully_whenEmailDoesNotExist() {
        RegisterRequest request = new RegisterRequest(
                "Mohamed",
                "mohamed@test.com",
                "12345678"
        );

        User user = new User();
        user.setName("Mohamed");
        user.setEmail("mohamed@test.com");
        user.setPasswordHash("hashed-password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Mohamed");
        savedUser.setEmail("mohamed@test.com");
        savedUser.setPasswordHash("hashed-password");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setName("Mohamed");
        expectedResponse.setEmail("mohamed@test.com");

        when(userRepository.existsByEmail("mohamed@test.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("hashed-password");
        when(registerMapper.registerToUser(request, "hashed-password")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(registerMapper.userToUserResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse result = authService.register(request);

        assertThat(result).isEqualTo(expectedResponse);

        verify(userRepository).existsByEmail("mohamed@test.com");
        verify(passwordEncoder).encode("12345678");
        verify(registerMapper).registerToUser(request, "hashed-password");
        verify(userRepository).save(user);
        verify(registerMapper).userToUserResponse(savedUser);
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Mohamed",
                "mohamed@test.com",
                "12345678"
        );

        when(userRepository.existsByEmail("mohamed@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail("mohamed@test.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(
                "mohamed@test.com",
                "12345678"
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("mohamed@test.com");
        user.setPasswordHash("hashed-password");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");
        refreshToken.setUser(user);

        when(userRepository.findByEmail("mohamed@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345678", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("mohamed@test.com")).thenReturn("access-token-value");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token-value");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-value");

        verify(userRepository).findByEmail("mohamed@test.com");
        verify(passwordEncoder).matches("12345678", "hashed-password");
        verify(jwtService).generateToken("mohamed@test.com");
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest(
                "wrong@test.com",
                "12345678"
        );

        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("wrong@test.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest(
                "mohamed@test.com",
                "wrong-password"
        );

        User user = new User();
        user.setEmail("mohamed@test.com");
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmail("mohamed@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("mohamed@test.com");
        verify(passwordEncoder).matches("wrong-password", "hashed-password");
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-value");

        User user = new User();
        user.setEmail("mohamed@test.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");
        refreshToken.setUser(user);

        when(refreshTokenService.verifyRefreshToken("refresh-token-value")).thenReturn(refreshToken);
        when(jwtService.generateToken("mohamed@test.com")).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-value");

        verify(refreshTokenService).verifyRefreshToken("refresh-token-value");
        verify(jwtService).generateToken("mohamed@test.com");
    }
}