package com.logifin.service.impl;

import com.logifin.dto.AuthResponse;
import com.logifin.dto.LoginRequest;
import com.logifin.dto.RegisterRequest;
import com.logifin.entity.Company;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.AuthService;
import com.logifin.service.CompanyAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService for authentication operations.
 * Handles login and registration with company admin assignment for first users.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CompanyAdminService companyAdminService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.debug("Processing login for email: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String roleName = userPrincipal.getAuthorities().isEmpty()
                ? null
                : userPrincipal.getAuthorities().iterator().next().getAuthority();

        // Check if user is company admin
        boolean isCompanyAdmin = companyAdminService.isUserCompanyAdmin(userPrincipal.getId());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .firstName(userPrincipal.getFirstName())
                .lastName(userPrincipal.getLastName())
                .role(roleName)
                .companyId(userPrincipal.getCompanyId())
                .companyName(userPrincipal.getCompanyName())
                .isCompanyAdmin(isCompanyAdmin)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        log.debug("Processing registration for email: {}", registerRequest.getEmail());

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("User", "email", registerRequest.getEmail());
        }

        // Find role by roleId if provided
        Role role = null;
        if (registerRequest.getRoleId() != null) {
            role = roleRepository.findById(registerRequest.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", registerRequest.getRoleId()));
        }

        // Find company by companyId if provided
        Company company = null;
        boolean isFirstUserForCompany = false;
        if (registerRequest.getCompanyId() != null) {
            company = companyRepository.findById(registerRequest.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", "id", registerRequest.getCompanyId()));

            // Check if this will be the first user for the company
            // This check happens BEFORE saving the user
            isFirstUserForCompany = !userRepository.existsByCompanyId(registerRequest.getCompanyId());
            log.debug("Company ID: {}, Is first user: {}", registerRequest.getCompanyId(), isFirstUserForCompany);
        }

        // Create user with role and company
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phone(registerRequest.getPhone())
                .active(true)
                .role(role)
                .company(company)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // If this is the first user for the company, assign them as company admin
        if (isFirstUserForCompany && company != null) {
            log.info("Assigning user {} as company admin for company {}", savedUser.getId(), company.getId());
            companyAdminService.assignFirstUserAsAdmin(company, savedUser);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        String roleName = savedUser.getRole() != null ? savedUser.getRole().getRoleName() : null;

        // Check if user is company admin
        boolean isCompanyAdmin = companyAdminService.isUserCompanyAdmin(savedUser.getId());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(roleName)
                .companyId(company != null ? company.getId() : null)
                .companyName(company != null ? company.getName() : null)
                .isCompanyAdmin(isCompanyAdmin)
                .build();
    }
}
