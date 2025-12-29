package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.AuthResponse;
import com.logifin.dto.LoginRequest;
import com.logifin.dto.RegisterRequest;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("john.doe@test.com")
                .firstName("John")
                .lastName("Doe")
                .role("ROLE_USER")
                .build();
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("john.doe@test.com")
                    .password("password123")
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@test.com"));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("john.doe@test.com")
                    .password("wrongpassword")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturnBadRequestForInvalidRequest() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("invalid-email")
                    .password("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Register Endpoint Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterSuccessfully() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@test.com")
                    .password("password123")
                    .phone("1234567890")
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                    .andExpect(jsonPath("$.data.email").value("john.doe@test.com"));
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void shouldReturnConflictForDuplicateEmail() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("existing@test.com")
                    .password("password123")
                    .build();

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new DuplicateResourceException("User", "email", "existing@test.com"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 for invalid registration data")
        void shouldReturnBadRequestForInvalidData() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("J")
                    .lastName("")
                    .email("invalid-email")
                    .password("123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}
