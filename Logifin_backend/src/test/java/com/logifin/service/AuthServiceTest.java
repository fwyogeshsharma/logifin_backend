package com.logifin.service;

import com.logifin.dto.AuthResponse;
import com.logifin.dto.LoginRequest;
import com.logifin.dto.RegisterRequest;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.entity.Wallet;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.repository.WalletRepository;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CompanyAdminService companyAdminService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private User testUserWithRole;
    private Role testRole;
    private UserPrincipal userPrincipal;
    private UserPrincipal userPrincipalWithRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .roleName("ROLE_USER")
                .description("User Role")
                .build();
        testRole.setId(1L);

        // User without role (new registration behavior)
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

        // User with role (for login tests)
        testUserWithRole = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("hashedPassword")
                .phone("1234567890")
                .active(true)
                .role(testRole)
                .build();
        testUserWithRole.setId(1L);

        userPrincipal = UserPrincipal.create(testUser);
        userPrincipalWithRole = UserPrincipal.create(testUserWithRole);
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with role")
        void shouldLoginSuccessfullyWithRole() {
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("john.doe@test.com")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipalWithRole);
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
            when(companyAdminService.isUserCompanyAdmin(anyLong())).thenReturn(false);

            AuthResponse result = authService.login(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("jwt-token");
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getRole()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("Should login successfully without role")
        void shouldLoginSuccessfullyWithoutRole() {
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("john.doe@test.com")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
            when(companyAdminService.isUserCompanyAdmin(anyLong())).thenReturn(false);

            AuthResponse result = authService.login(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("jwt-token");
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
            assertThat(result.getRole()).isNull();
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user with default role and wallet")
        void shouldRegisterUserWithDefaultRoleAndWallet() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@test.com")
                    .password("password123")
                    .phone("1234567890")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(walletRepository.existsByUserId(anyLong())).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
            when(companyAdminService.isUserCompanyAdmin(anyLong())).thenReturn(false);

            AuthResponse result = authService.register(registerRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("jwt-token");
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
            verify(userRepository).save(any(User.class));
            verify(walletRepository).save(any(Wallet.class));
            verify(roleRepository).findByRoleName("ROLE_USER");
        }

        @Test
        @DisplayName("Should create wallet during registration")
        void shouldCreateWalletDuringRegistration() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@test.com")
                    .password("password123")
                    .phone("1234567890")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(walletRepository.existsByUserId(anyLong())).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
            when(companyAdminService.isUserCompanyAdmin(anyLong())).thenReturn(false);

            authService.register(registerRequest);

            ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
            verify(walletRepository).save(walletCaptor.capture());

            Wallet createdWallet = walletCaptor.getValue();
            assertThat(createdWallet.getUserId()).isEqualTo(testUser.getId());
            assertThat(createdWallet.getCurrencyCode()).isEqualTo("INR");
            assertThat(createdWallet.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("existing@test.com")
                    .password("password123")
                    .build();

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("Should register user with roleId in register request")
        void shouldRegisterUserWithRoleId() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@test.com")
                    .password("password123")
                    .phone("1234567890")
                    .roleId(1L)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(walletRepository.existsByUserId(anyLong())).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
            when(companyAdminService.isUserCompanyAdmin(anyLong())).thenReturn(false);

            AuthResponse result = authService.register(registerRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("jwt-token");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("John");
            verify(roleRepository).findById(1L);
            verify(walletRepository).save(any(Wallet.class));
        }
    }
}
