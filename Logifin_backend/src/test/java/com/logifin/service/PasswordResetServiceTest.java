package com.logifin.service;

import com.logifin.config.EmailConfig;
import com.logifin.dto.ForgotPasswordRequest;
import com.logifin.dto.ResetPasswordRequest;
import com.logifin.entity.PasswordResetToken;
import com.logifin.entity.PasswordResetToken.TokenStatus;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.InvalidTokenException;
import com.logifin.exception.TokenExpiredException;
import com.logifin.repository.PasswordResetTokenRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService Tests")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailConfig emailConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User testUser;
    private Role testRole;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .roleName("ROLE_CSR")
                .description("CSR Role")
                .build();
        testRole.setId(1L);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("hashedPassword")
                .phone("1234567890")
                .active(true)
                .role(testRole)
                .build();
        testUser.setId(1L);

        testToken = PasswordResetToken.builder()
                .user(testUser)
                .token("valid-test-token")
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .status(TokenStatus.ACTIVE)
                .build();
        testToken.setId(1L);
    }

    @Nested
    @DisplayName("Forgot Password Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should process forgot password for existing user")
        void shouldProcessForgotPasswordForExistingUser() {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("john.doe@test.com")
                    .build();

            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
            when(tokenRepository.invalidateUserTokens(testUser)).thenReturn(0);
            when(emailConfig.getTokenExpirationMinutes()).thenReturn(30);
            when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

            passwordResetService.processForgotPassword(request);

            verify(userRepository).findByEmail("john.doe@test.com");
            verify(tokenRepository).invalidateUserTokens(testUser);
            verify(tokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(eq("john.doe@test.com"), eq("John Doe"), anyString());
        }

        @Test
        @DisplayName("Should not reveal non-existent user")
        void shouldNotRevealNonExistentUser() {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("nonexistent@test.com")
                    .build();

            when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

            // Should complete without exception
            passwordResetService.processForgotPassword(request);

            verify(userRepository).findByEmail("nonexistent@test.com");
            verify(tokenRepository, never()).save(any(PasswordResetToken.class));
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should not process forgot password for inactive user")
        void shouldNotProcessForgotPasswordForInactiveUser() {
            testUser.setActive(false);
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("john.doe@test.com")
                    .build();

            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));

            passwordResetService.processForgotPassword(request);

            verify(userRepository).findByEmail("john.doe@test.com");
            verify(tokenRepository, never()).save(any(PasswordResetToken.class));
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should invalidate existing tokens before creating new one")
        void shouldInvalidateExistingTokensBeforeCreatingNewOne() {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("john.doe@test.com")
                    .build();

            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
            when(tokenRepository.invalidateUserTokens(testUser)).thenReturn(2);
            when(emailConfig.getTokenExpirationMinutes()).thenReturn(30);
            when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

            passwordResetService.processForgotPassword(request);

            verify(tokenRepository).invalidateUserTokens(testUser);
            verify(tokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Should create token with correct expiration time")
        void shouldCreateTokenWithCorrectExpirationTime() {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("john.doe@test.com")
                    .build();

            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
            when(tokenRepository.invalidateUserTokens(testUser)).thenReturn(0);
            when(emailConfig.getTokenExpirationMinutes()).thenReturn(30);
            when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

            passwordResetService.processForgotPassword(request);

            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            PasswordResetToken capturedToken = tokenCaptor.getValue();
            assertThat(capturedToken.getUser()).isEqualTo(testUser);
            assertThat(capturedToken.getStatus()).isEqualTo(TokenStatus.ACTIVE);
            assertThat(capturedToken.getExpiryDate()).isAfter(LocalDateTime.now());
            assertThat(capturedToken.getExpiryDate()).isBefore(LocalDateTime.now().plusMinutes(31));
        }

        @Test
        @DisplayName("Should handle email case insensitively")
        void shouldHandleEmailCaseInsensitively() {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("JOHN.DOE@TEST.COM")
                    .build();

            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
            when(tokenRepository.invalidateUserTokens(testUser)).thenReturn(0);
            when(emailConfig.getTokenExpirationMinutes()).thenReturn(30);
            when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

            passwordResetService.processForgotPassword(request);

            verify(userRepository).findByEmail("john.doe@test.com");
        }
    }

    @Nested
    @DisplayName("Validate Token Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for valid token")
        void shouldReturnTrueForValidToken() {
            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));

            boolean result = passwordResetService.validateToken("valid-test-token");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existent token")
        void shouldReturnFalseForNonExistentToken() {
            when(tokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

            boolean result = passwordResetService.validateToken("non-existent-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));

            boolean result = passwordResetService.validateToken("valid-test-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for used token")
        void shouldReturnFalseForUsedToken() {
            testToken.setStatus(TokenStatus.USED);
            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));

            boolean result = passwordResetService.validateToken("valid-test-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null token")
        void shouldReturnFalseForNullToken() {
            boolean result = passwordResetService.validateToken(null);

            assertThat(result).isFalse();
            verify(tokenRepository, never()).findByToken(anyString());
        }

        @Test
        @DisplayName("Should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            boolean result = passwordResetService.validateToken("   ");

            assertThat(result).isFalse();
            verify(tokenRepository, never()).findByToken(anyString());
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-test-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));
            when(passwordEncoder.encode("NewPassword1!")).thenReturn("encodedNewPassword");
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(tokenRepository.save(testToken)).thenReturn(testToken);

            passwordResetService.resetPassword(request);

            verify(passwordEncoder).encode("NewPassword1!");
            verify(userRepository).save(testUser);
            verify(tokenRepository).save(testToken);
            verify(emailService).sendPasswordResetConfirmationEmail("john.doe@test.com", "John Doe");

            assertThat(testUser.getPassword()).isEqualTo("encodedNewPassword");
            assertThat(testToken.getStatus()).isEqualTo(TokenStatus.USED);
        }

        @Test
        @DisplayName("Should throw exception when passwords do not match")
        void shouldThrowExceptionWhenPasswordsDoNotMatch() {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-test-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("DifferentPassword1!")
                    .build();

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Passwords do not match");
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("invalid-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Invalid or expired password reset token");
        }

        @Test
        @DisplayName("Should throw exception for already used token")
        void shouldThrowExceptionForAlreadyUsedToken() {
            testToken.setStatus(TokenStatus.USED);
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-test-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("already been used");
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void shouldThrowExceptionForExpiredToken() {
            testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-test-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));
            when(tokenRepository.save(testToken)).thenReturn(testToken);

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("Should throw exception for token marked as expired")
        void shouldThrowExceptionForTokenMarkedAsExpired() {
            testToken.setStatus(TokenStatus.EXPIRED);
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-test-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("Should mark token as expired when it is expired")
        void shouldMarkTokenAsExpiredWhenItIsExpired() {
            testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-test-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            when(tokenRepository.findByToken("valid-test-token")).thenReturn(Optional.of(testToken));
            when(tokenRepository.save(testToken)).thenReturn(testToken);

            assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                    .isInstanceOf(TokenExpiredException.class);

            assertThat(testToken.getStatus()).isEqualTo(TokenStatus.EXPIRED);
            verify(tokenRepository).save(testToken);
        }
    }
}
