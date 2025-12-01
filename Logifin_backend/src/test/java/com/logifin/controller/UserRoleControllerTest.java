package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.SetRoleRequest;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserRoleController Tests")
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserRoleService userRoleService;

    private String adminToken;
    private String superAdminToken;
    private String csrToken;

    @BeforeEach
    void setUp() {
        adminToken = createTokenForRole("ROLE_ADMIN", 1L, "admin@test.com");
        superAdminToken = createTokenForRole("ROLE_SUPER_ADMIN", 2L, "superadmin@test.com");
        csrToken = createTokenForRole("ROLE_CSR", 3L, "csr@test.com");
    }

    private String createTokenForRole(String roleName, Long userId, String email) {
        Role role = Role.builder().roleName(roleName).build();
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("password")
                .active(true)
                .role(role)
                .build();
        user.setId(userId);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, Collections.singletonList(new SimpleGrantedAuthority(roleName)));
        return tokenProvider.generateToken(authentication);
    }

    @Nested
    @DisplayName("Set Role Endpoint Tests")
    class SetRoleTests {

        @Test
        @DisplayName("Should set role successfully as ADMIN")
        void shouldSetRoleSuccessfullyAsAdmin() throws Exception {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(10L)
                    .roleId(1L)
                    .build();

            doNothing().when(userRoleService).setUserRole(any(SetRoleRequest.class));

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User role updated successfully"));

            verify(userRoleService).setUserRole(any(SetRoleRequest.class));
        }

        @Test
        @DisplayName("Should set role successfully as SUPER_ADMIN")
        void shouldSetRoleSuccessfullyAsSuperAdmin() throws Exception {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(10L)
                    .roleId(1L)
                    .build();

            doNothing().when(userRoleService).setUserRole(any(SetRoleRequest.class));

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User role updated successfully"));
        }

        @Test
        @DisplayName("Should return 403 for CSR role")
        void shouldReturnForbiddenForCsrRole() throws Exception {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(10L)
                    .roleId(1L)
                    .build();

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + csrToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(userRoleService, never()).setUserRole(any());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturnUnauthorizedWithoutAuth() throws Exception {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(10L)
                    .roleId(1L)
                    .build();

            mockMvc.perform(post("/api/v1/user/set-role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(userRoleService, never()).setUserRole(any());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturnNotFoundWhenUserNotFound() throws Exception {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(999L)
                    .roleId(1L)
                    .build();

            doThrow(new ResourceNotFoundException("User", "id", 999L))
                    .when(userRoleService).setUserRole(any(SetRoleRequest.class));

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 404 when role not found")
        void shouldReturnNotFoundWhenRoleNotFound() throws Exception {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(10L)
                    .roleId(999L)
                    .build();

            doThrow(new ResourceNotFoundException("Role", "id", 999L))
                    .when(userRoleService).setUserRole(any(SetRoleRequest.class));

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 for null userId")
        void shouldReturnBadRequestForNullUserId() throws Exception {
            String requestJson = "{\"roleId\": 1}";

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for null roleId")
        void shouldReturnBadRequestForNullRoleId() throws Exception {
            String requestJson = "{\"userId\": 10}";

            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty request body")
        void shouldReturnBadRequestForEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/user/set-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
