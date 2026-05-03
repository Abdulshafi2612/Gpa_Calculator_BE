package com.example.project.gpa_calculator.service.impl;

import com.example.project.gpa_calculator.entity.RefreshToken;
import com.example.project.gpa_calculator.entity.User;
import com.example.project.gpa_calculator.exception.InvalidCredentialsException;
import com.example.project.gpa_calculator.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void createRefreshToken_shouldCreateNewRefreshToken_whenUserHasNoExistingToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("mohamed@test.com");

        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());

        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_shouldUpdateExistingRefreshToken_whenUserAlreadyHasToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("mohamed@test.com");

        RefreshToken existingToken = new RefreshToken();
        existingToken.setId(10L);
        existingToken.setUser(user);
        existingToken.setToken("old-token");
        existingToken.setExpiryDate(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));

        when(refreshTokenRepository.save(existingToken)).thenReturn(existingToken);

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertThat(result).isEqualTo(existingToken);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getToken()).isNotEqualTo("old-token");
        assertThat(result.getExpiryDate()).isAfter(Instant.now());

        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).save(existingToken);
    }

    @Test
    void verifyRefreshToken_shouldReturnRefreshToken_whenTokenExistsAndNotExpired() {
        String token = "valid-refresh-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken(token);

        assertThat(result).isEqualTo(refreshToken);

        verify(refreshTokenRepository).findByToken(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyRefreshToken_shouldThrowException_whenTokenDoesNotExist() {
        String token = "invalid-refresh-token";

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken(token))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenRepository).findByToken(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyRefreshToken_shouldDeleteAndThrowException_whenTokenIsExpired() {
        String token = "expired-refresh-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenRepository).findByToken(token);
        verify(refreshTokenRepository).delete(refreshToken);
    }
}