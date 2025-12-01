package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.RoleDTO;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.RoleService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RoleController Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockBean
    private RoleService roleService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    private RoleDTO testRoleDTO;
    private String superAdminToken;
    private String adminToken;
    private String csrToken;

    @BeforeEach
    void setUp() {
        testRoleDTO = RoleDTO.builder()
                .id(1L)
                .roleName("ROLE_TEST")
                .description("Test Role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        superAdminToken = createTokenForRole("ROLE_SUPER_ADMIN", 1L, "superadmin@test.com");
        adminToken = createTokenForRole("ROLE_ADMIN", 2L, "admin@test.com");
        csrToken = createTokenForRole("ROLE_CSR", 3L, "csr@test.com");
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
    @DisplayName("GET /api/v1/roles Tests")
    class GetRolesTests {

        @Test
        @DisplayName("Should get all roles with Super Admin role")
        void shouldGetAllRolesWithSuperAdminRole() throws Exception {
            when(roleService.getAllRoles()).thenReturn(Arrays.asList(testRoleDTO));

            mockMvc.perform(get("/api/v1/roles")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].roleName").value("ROLE_TEST"));
        }

        @Test
        @DisplayName("Should return 403 for Admin role")
        void shouldReturn403ForAdminRole() throws Exception {
            mockMvc.perform(get("/api/v1/roles")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 for CSR role")
        void shouldReturn403ForCsrRole() throws Exception {
            mockMvc.perform(get("/api/v1/roles")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturnUnauthorizedWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/roles"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should get role by ID")
        void shouldGetRoleById() throws Exception {
            when(roleService.getRoleById(1L)).thenReturn(testRoleDTO);

            mockMvc.perform(get("/api/v1/roles/1")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("Should return 404 for non-existent role")
        void shouldReturn404ForNonExistentRole() throws Exception {
            when(roleService.getRoleById(999L))
                    .thenThrow(new ResourceNotFoundException("Role", "id", 999L));

            mockMvc.perform(get("/api/v1/roles/999")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get role by name")
        void shouldGetRoleByName() throws Exception {
            when(roleService.getRoleByName("ROLE_TEST")).thenReturn(testRoleDTO);

            mockMvc.perform(get("/api/v1/roles/name/ROLE_TEST")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.roleName").value("ROLE_TEST"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/roles Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role with Super Admin role")
        void shouldCreateRoleWithSuperAdminRole() throws Exception {
            RoleDTO createDTO = RoleDTO.builder()
                    .roleName("ROLE_NEW")
                    .description("New Role")
                    .build();

            when(roleService.createRole(any(RoleDTO.class))).thenReturn(testRoleDTO);

            mockMvc.perform(post("/api/v1/roles")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for Admin role trying to create")
        void shouldReturn403ForAdminRoleCreate() throws Exception {
            RoleDTO createDTO = RoleDTO.builder()
                    .roleName("ROLE_NEW")
                    .description("New Role")
                    .build();

            mockMvc.perform(post("/api/v1/roles")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 409 for duplicate role name")
        void shouldReturn409ForDuplicateRoleName() throws Exception {
            RoleDTO createDTO = RoleDTO.builder()
                    .roleName("ROLE_EXISTING")
                    .description("Existing Role")
                    .build();

            when(roleService.createRole(any(RoleDTO.class)))
                    .thenThrow(new DuplicateResourceException("Role", "name", "ROLE_EXISTING"));

            mockMvc.perform(post("/api/v1/roles")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/roles/{id} Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update role with Super Admin role")
        void shouldUpdateRoleWithSuperAdminRole() throws Exception {
            RoleDTO updateDTO = RoleDTO.builder()
                    .roleName("ROLE_UPDATED")
                    .description("Updated Description")
                    .build();

            when(roleService.updateRole(anyLong(), any(RoleDTO.class))).thenReturn(testRoleDTO);

            mockMvc.perform(put("/api/v1/roles/1")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for Admin role trying to update")
        void shouldReturn403ForAdminRoleUpdate() throws Exception {
            RoleDTO updateDTO = RoleDTO.builder()
                    .roleName("ROLE_UPDATED")
                    .description("Updated Description")
                    .build();

            mockMvc.perform(put("/api/v1/roles/1")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/roles/{id} Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role with Super Admin role")
        void shouldDeleteRoleWithSuperAdminRole() throws Exception {
            doNothing().when(roleService).deleteRole(1L);

            mockMvc.perform(delete("/api/v1/roles/1")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for Admin role trying to delete")
        void shouldReturn403ForAdminRoleDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/roles/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent role")
        void shouldReturn404ForNonExistentRoleDelete() throws Exception {
            doThrow(new ResourceNotFoundException("Role", "id", 999L))
                    .when(roleService).deleteRole(999L);

            mockMvc.perform(delete("/api/v1/roles/999")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
