package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.ForgotPasswordRequest;
import com.logifin.dto.ResetPasswordRequest;
import com.logifin.dto.ValidateTokenRequest;
import com.logifin.exception.InvalidTokenException;
import com.logifin.exception.TokenExpiredException;
import com.logifin.service.PasswordResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PasswordResetController Tests")
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordResetService passwordResetService;

    @Nested
    @DisplayName("Forgot Password Endpoint Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should process forgot password request successfully")
        void shouldProcessForgotPasswordSuccessfully() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("john.doe@test.com")
                    .build();

            doNothing().when(passwordResetService).processForgotPassword(any(ForgotPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("If the email exists in our system, you will receive a password reset link shortly"));
        }

        @Test
        @DisplayName("Should return success even for non-existent email (security)")
        void shouldReturnSuccessForNonExistentEmail() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("nonexistent@test.com")
                    .build();

            doNothing().when(passwordResetService).processForgotPassword(any(ForgotPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturnBadRequestForInvalidEmail() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("invalid-email")
                    .build();

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty email")
        void shouldReturnBadRequestForEmptyEmail() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for null email")
        void shouldReturnBadRequestForNullEmail() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Validate Reset Token Endpoint Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return valid=true for valid token")
        void shouldReturnValidTrueForValidToken() throws Exception {
            ValidateTokenRequest request = ValidateTokenRequest.builder()
                    .token("valid-token")
                    .build();

            when(passwordResetService.validateToken("valid-token")).thenReturn(true);

            mockMvc.perform(post("/api/v1/auth/validate-reset-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Token is valid"))
                    .andExpect(jsonPath("$.data.valid").value(true));
        }

        @Test
        @DisplayName("Should return valid=false for invalid token")
        void shouldReturnValidFalseForInvalidToken() throws Exception {
            ValidateTokenRequest request = ValidateTokenRequest.builder()
                    .token("invalid-token")
                    .build();

            when(passwordResetService.validateToken("invalid-token")).thenReturn(false);

            mockMvc.perform(post("/api/v1/auth/validate-reset-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Token is invalid or expired"))
                    .andExpect(jsonPath("$.data.valid").value(false));
        }

        @Test
        @DisplayName("Should return 400 for empty token")
        void shouldReturnBadRequestForEmptyToken() throws Exception {
            ValidateTokenRequest request = ValidateTokenRequest.builder()
                    .token("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/validate-reset-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for null token")
        void shouldReturnBadRequestForNullToken() throws Exception {
            ValidateTokenRequest request = new ValidateTokenRequest();

            mockMvc.perform(post("/api/v1/auth/validate-reset-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Reset Password Endpoint Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            doNothing().when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password has been reset successfully"));
        }

        @Test
        @DisplayName("Should return 400 for invalid token")
        void shouldReturnBadRequestForInvalidToken() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("invalid-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            doThrow(new InvalidTokenException("Invalid or expired password reset token"))
                    .when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
        }

        @Test
        @DisplayName("Should return 400 for expired token")
        void shouldReturnBadRequestForExpiredToken() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("expired-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            doThrow(new TokenExpiredException("This password reset link has expired"))
                    .when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("This password reset link has expired"));
        }

        @Test
        @DisplayName("Should return 400 for passwords not matching")
        void shouldReturnBadRequestForPasswordsNotMatching() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("NewPassword1!")
                    .confirmPassword("DifferentPassword1!")
                    .build();

            doThrow(new InvalidTokenException("Passwords do not match"))
                    .when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 for weak password")
        void shouldReturnBadRequestForWeakPassword() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("weak")
                    .confirmPassword("weak")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty token")
        void shouldReturnBadRequestForEmptyToken() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("")
                    .newPassword("NewPassword1!")
                    .confirmPassword("NewPassword1!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty password")
        void shouldReturnBadRequestForEmptyPassword() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("")
                    .confirmPassword("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for password without special character")
        void shouldReturnBadRequestForPasswordWithoutSpecialChar() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("NewPassword1")
                    .confirmPassword("NewPassword1")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
