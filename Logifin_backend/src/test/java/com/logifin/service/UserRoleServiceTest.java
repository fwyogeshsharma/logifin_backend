package com.logifin.service;

import com.logifin.dto.SetRoleRequest;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.impl.UserRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleService Tests")
class UserRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private User testUser;
    private Role testRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .roleName("ROLE_CSR")
                .description("CSR Role")
                .build();
        testRole.setId(1L);

        adminRole = Role.builder()
                .roleName("ROLE_ADMIN")
                .description("Admin Role")
                .build();
        adminRole.setId(2L);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("hashedPassword")
                .phone("1234567890")
                .active(true)
                .role(null)
                .build();
        testUser.setId(1L);
    }

    @Nested
    @DisplayName("Set User Role Tests")
    class SetUserRoleTests {

        @Test
        @DisplayName("Should set role for user successfully")
        void shouldSetRoleForUserSuccessfully() {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(1L)
                    .roleId(1L)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userRoleService.setUserRole(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(testRole);
            assertThat(savedUser.getRole().getRoleName()).isEqualTo("ROLE_CSR");
        }

        @Test
        @DisplayName("Should update existing role")
        void shouldUpdateExistingRole() {
            testUser.setRole(testRole);

            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(1L)
                    .roleId(2L)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findById(2L)).thenReturn(Optional.of(adminRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userRoleService.setUserRole(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(adminRole);
            assertThat(savedUser.getRole().getRoleName()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(999L)
                    .roleId(1L)
                    .build();

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userRoleService.setUserRole(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("999");

            verify(roleRepository, never()).findById(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when role not found")
        void shouldThrowExceptionWhenRoleNotFound() {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(1L)
                    .roleId(999L)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userRoleService.setUserRole(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Role")
                    .hasMessageContaining("999");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should call repositories in correct order")
        void shouldCallRepositoriesInCorrectOrder() {
            SetRoleRequest request = SetRoleRequest.builder()
                    .userId(1L)
                    .roleId(1L)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userRoleService.setUserRole(request);

            InOrder inOrderVerifier = inOrder(userRepository, roleRepository);
            inOrderVerifier.verify(userRepository).findById(1L);
            inOrderVerifier.verify(roleRepository).findById(1L);
            inOrderVerifier.verify(userRepository).save(any(User.class));
        }
    }
}
