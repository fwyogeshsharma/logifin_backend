package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.UserDTO;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    private UserDTO testUserDTO;
    private String adminToken;
    private String superAdminToken;
    private String csrToken;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .phone("1234567890")
                .active(true)
                .roleId(1L)
                .roleName("ROLE_CSR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create tokens for different roles
        csrToken = createTokenForRole("ROLE_CSR", 1L, "csr@test.com");
        adminToken = createTokenForRole("ROLE_ADMIN", 2L, "admin@test.com");
        superAdminToken = createTokenForRole("ROLE_SUPER_ADMIN", 3L, "superadmin@test.com");
    }

    private String createTokenForRole(String roleName, Long userId, String email) {
        Role role = Role.builder().roleName(roleName).build();
        role.setId(1L);
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
    @DisplayName("GET /api/v1/users Tests")
    class GetUsersTests {

        @Test
        @DisplayName("Should get all users with CSR role")
        void shouldGetAllUsersWithCsrRole() throws Exception {
            when(userService.getAllUsers()).thenReturn(Arrays.asList(testUserDTO));

            mockMvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].email").value("john.doe@test.com"));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturnUnauthorizedWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetUserById() throws Exception {
            when(userService.getUserById(1L)).thenReturn(testUserDTO);

            mockMvc.perform(get("/api/v1/users/1")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() throws Exception {
            when(userService.getUserById(999L))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            mockMvc.perform(get("/api/v1/users/999")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get user by email")
        void shouldGetUserByEmail() throws Exception {
            when(userService.getUserByEmail("john.doe@test.com")).thenReturn(testUserDTO);

            mockMvc.perform(get("/api/v1/users/email/john.doe@test.com")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("john.doe@test.com"));
        }

        @Test
        @DisplayName("Should get active users")
        void shouldGetActiveUsers() throws Exception {
            when(userService.getActiveUsers()).thenReturn(Arrays.asList(testUserDTO));

            mockMvc.perform(get("/api/v1/users/active")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].active").value(true));
        }

        @Test
        @DisplayName("Should search users by name")
        void shouldSearchUsersByName() throws Exception {
            when(userService.searchUsersByName("John")).thenReturn(Arrays.asList(testUserDTO));

            mockMvc.perform(get("/api/v1/users/search")
                            .param("name", "John")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].firstName").value("John"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{id} Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user with Admin role")
        void shouldUpdateUserWithAdminRole() throws Exception {
            UserDTO updateDTO = UserDTO.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .email("john.doe@test.com")
                    .build();

            when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(testUserDTO);

            mockMvc.perform(put("/api/v1/users/1")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to update")
        void shouldReturn403ForCsrRoleUpdate() throws Exception {
            UserDTO updateDTO = UserDTO.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .email("john.doe@test.com")
                    .build();

            mockMvc.perform(put("/api/v1/users/1")
                            .header("Authorization", "Bearer " + csrToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id} Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user with Super Admin role")
        void shouldDeleteUserWithSuperAdminRole() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/v1/users/1")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for Admin role trying to delete")
        void shouldReturn403ForAdminRoleDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/users/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to delete")
        void shouldReturn403ForCsrRoleDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/users/1")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/activate|deactivate Tests")
    class ActivateDeactivateTests {

        @Test
        @DisplayName("Should activate user with Admin role")
        void shouldActivateUserWithAdminRole() throws Exception {
            doNothing().when(userService).activateUser(1L);

            mockMvc.perform(patch("/api/v1/users/1/activate")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should deactivate user with Admin role")
        void shouldDeactivateUserWithAdminRole() throws Exception {
            doNothing().when(userService).deactivateUser(1L);

            mockMvc.perform(patch("/api/v1/users/1/deactivate")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to activate")
        void shouldReturn403ForCsrRoleActivate() throws Exception {
            mockMvc.perform(patch("/api/v1/users/1/activate")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isForbidden());
        }
    }
}
