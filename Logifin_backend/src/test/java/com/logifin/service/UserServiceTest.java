package com.logifin.service;

import com.logifin.dto.UserDTO;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.entity.Wallet;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.repository.WalletRepository;
import com.logifin.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role testRole;

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
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .phone("1234567890")
                .active(true)
                .roleId(1L)
                .roleName("ROLE_CSR")
                .build();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_CSR")).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(walletRepository.existsByUserId(anyLong())).thenReturn(false);
            when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserDTO result = userService.createUser(testUserDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
            verify(userRepository).save(any(User.class));
            verify(walletRepository).existsByUserId(1L);
            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should create user and wallet with INR currency")
        void shouldCreateUserAndWalletWithINRCurrency() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_CSR")).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(walletRepository.existsByUserId(anyLong())).thenReturn(false);

            userService.createUser(testUserDTO);

            verify(walletRepository).save(argThat(wallet ->
                    wallet.getUserId().equals(1L) &&
                    wallet.getCurrencyCode().equals("INR") &&
                    wallet.getStatus().equals("ACTIVE")
            ));
        }

        @Test
        @DisplayName("Should not create duplicate wallet if already exists")
        void shouldNotCreateDuplicateWallet() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_CSR")).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(walletRepository.existsByUserId(anyLong())).thenReturn(true);

            userService.createUser(testUserDTO);

            verify(walletRepository).existsByUserId(1L);
            verify(walletRepository, never()).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(testUserDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDTO result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowExceptionWhenUserNotFoundById() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Should get user by email successfully")
        void shouldGetUserByEmailSuccessfully() {
            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));

            UserDTO result = userService.getUserByEmail("john.doe@test.com");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Should get all users")
        void shouldGetAllUsers() {
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Should get active users")
        void shouldGetActiveUsers() {
            when(userRepository.findByActiveTrue()).thenReturn(Arrays.asList(testUser));

            List<UserDTO> result = userService.getActiveUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Should search users by name")
        void shouldSearchUsersByName() {
            when(userRepository.searchByName("John")).thenReturn(Arrays.asList(testUser));

            List<UserDTO> result = userService.searchUsersByName("John");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            UserDTO updateDTO = UserDTO.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .email("john.doe@test.com")
                    .phone("9876543210")
                    .active(true)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDTO result = userService.updateUser(1L, updateDTO);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when updating to duplicate email")
        void shouldThrowExceptionWhenUpdatingToDuplicateEmail() {
            UserDTO updateDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("existing@test.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent user")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Activate/Deactivate User Tests")
    class ActivateDeactivateTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.deactivateUser(1L);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUserSuccessfully() {
            testUser.setActive(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.activateUser(1L);

            verify(userRepository).save(any(User.class));
        }
    }
}
