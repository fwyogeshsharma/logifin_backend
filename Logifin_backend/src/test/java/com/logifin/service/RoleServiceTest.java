package com.logifin.service;

import com.logifin.dto.RoleDTO;
import com.logifin.entity.Role;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private RoleDTO testRoleDTO;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .roleName("ROLE_ADMIN")
                .description("Administrator Role")
                .build();
        testRole.setId(1L);
        testRole.setCreatedAt(LocalDateTime.now());
        testRole.setUpdatedAt(LocalDateTime.now());

        testRoleDTO = RoleDTO.builder()
                .id(1L)
                .roleName("ROLE_ADMIN")
                .description("Administrator Role")
                .build();
    }

    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully")
        void shouldCreateRoleSuccessfully() {
            when(roleRepository.existsByRoleName(anyString())).thenReturn(false);
            when(roleRepository.save(any(Role.class))).thenReturn(testRole);

            RoleDTO result = roleService.createRole(testRoleDTO);

            assertThat(result).isNotNull();
            assertThat(result.getRoleName()).isEqualTo("ROLE_ADMIN");
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("Should throw exception when role name already exists")
        void shouldThrowExceptionWhenRoleNameExists() {
            when(roleRepository.existsByRoleName(anyString())).thenReturn(true);

            assertThatThrownBy(() -> roleService.createRole(testRoleDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("name");
        }
    }

    @Nested
    @DisplayName("Get Role Tests")
    class GetRoleTests {

        @Test
        @DisplayName("Should get role by ID successfully")
        void shouldGetRoleByIdSuccessfully() {
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

            RoleDTO result = roleService.getRoleById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getRoleName()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should throw exception when role not found by ID")
        void shouldThrowExceptionWhenRoleNotFoundById() {
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.getRoleById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Role");
        }

        @Test
        @DisplayName("Should get role by name successfully")
        void shouldGetRoleByNameSuccessfully() {
            when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(testRole));

            RoleDTO result = roleService.getRoleByName("ROLE_ADMIN");

            assertThat(result).isNotNull();
            assertThat(result.getRoleName()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should get all roles")
        void shouldGetAllRoles() {
            Role anotherRole = Role.builder()
                    .roleName("ROLE_USER")
                    .description("User Role")
                    .build();
            anotherRole.setId(2L);

            when(roleRepository.findAll()).thenReturn(Arrays.asList(testRole, anotherRole));

            List<RoleDTO> result = roleService.getAllRoles();

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update Role Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update role successfully")
        void shouldUpdateRoleSuccessfully() {
            RoleDTO updateDTO = RoleDTO.builder()
                    .roleName("ROLE_ADMIN")
                    .description("Updated Description")
                    .build();

            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.save(any(Role.class))).thenReturn(testRole);

            RoleDTO result = roleService.updateRole(1L, updateDTO);

            assertThat(result).isNotNull();
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("Should throw exception when updating to duplicate role name")
        void shouldThrowExceptionWhenUpdatingToDuplicateRoleName() {
            RoleDTO updateDTO = RoleDTO.builder()
                    .roleName("ROLE_EXISTING")
                    .description("Description")
                    .build();

            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(roleRepository.existsByRoleName("ROLE_EXISTING")).thenReturn(true);

            assertThatThrownBy(() -> roleService.updateRole(1L, updateDTO))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete Role Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role successfully")
        void shouldDeleteRoleSuccessfully() {
            when(roleRepository.existsById(1L)).thenReturn(true);
            doNothing().when(roleRepository).deleteById(1L);

            roleService.deleteRole(1L);

            verify(roleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent role")
        void shouldThrowExceptionWhenDeletingNonExistentRole() {
            when(roleRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> roleService.deleteRole(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
