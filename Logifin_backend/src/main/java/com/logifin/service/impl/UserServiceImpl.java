package com.logifin.service.impl;

import com.logifin.config.CacheConfig;
import com.logifin.dto.PagedResponse;
import com.logifin.dto.UserDTO;
import com.logifin.entity.Company;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.entity.Wallet;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.repository.WalletRepository;
import com.logifin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    private static final String DEFAULT_ROLE = "ROLE_CSR";

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USERS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_ACTIVE_USERS, allEntries = true)
    })
    public UserDTO createUser(UserDTO userDTO) {
        log.debug("Creating user with email: {}", userDTO.getEmail());
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDTO.getEmail());
        }

        Role role = null;
        if (userDTO.getRoleId() != null) {
            role = roleRepository.findById(userDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", userDTO.getRoleId()));
        } else if (userDTO.getRoleName() != null) {
            role = roleRepository.findByRoleName(userDTO.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", userDTO.getRoleName()));
        } else {
            role = roleRepository.findByRoleName(DEFAULT_ROLE)
                    .orElse(null);
        }

        Company company = null;
        if (userDTO.getCompanyId() != null) {
            company = companyRepository.findById(userDTO.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", "id", userDTO.getCompanyId()));
        }

        User user = mapToEntity(userDTO);
        user.setRole(role);
        user.setCompany(company);
        User savedUser = userRepository.save(user);

        // Create default wallet for the user
        createDefaultWallet(savedUser.getId());

        return mapToDTO(savedUser);
    }

    /**
     * Creates a default wallet for a user with INR currency
     */
    private void createDefaultWallet(Long userId) {
        try {
            if (!walletRepository.existsByUserId(userId)) {
                Wallet wallet = Wallet.builder()
                        .userId(userId)
                        .currencyCode("INR")
                        .status("ACTIVE")
                        .build();
                walletRepository.save(wallet);
                log.info("Created default wallet for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to create wallet for user: {}", userId, e);
            // Don't throw exception - wallet creation failure shouldn't fail user creation
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_USER_BY_ID, key = "#id", unless = "#result == null")
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user by id: {} from database", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_USER_BY_EMAIL, key = "#email", unless = "#result == null")
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {} from database", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_USERS, unless = "#result == null || #result.isEmpty()")
    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users from database");
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_ACTIVE_USERS, unless = "#result == null || #result.isEmpty()")
    public List<UserDTO> getActiveUsers() {
        log.debug("Fetching active users from database");
        return userRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_USER_SEARCH, key = "#name", unless = "#result == null || #result.isEmpty()")
    public List<UserDTO> searchUsersByName(String name) {
        log.debug("Searching users by name: {} from database", name);
        return userRepository.searchByName(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USER_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_USER_BY_EMAIL, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_USERS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_ACTIVE_USERS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_USER_SEARCH, allEntries = true)
    })
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.debug("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!existingUser.getEmail().equals(userDTO.getEmail())
                && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDTO.getEmail());
        }

        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone(userDTO.getPhone());

        if (userDTO.getActive() != null) {
            existingUser.setActive(userDTO.getActive());
        }

        if (userDTO.getRoleId() != null) {
            Role role = roleRepository.findById(userDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", userDTO.getRoleId()));
            existingUser.setRole(role);
        } else if (userDTO.getRoleName() != null) {
            Role role = roleRepository.findByRoleName(userDTO.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", userDTO.getRoleName()));
            existingUser.setRole(role);
        }

        if (userDTO.getCompanyId() != null) {
            Company company = companyRepository.findById(userDTO.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", "id", userDTO.getCompanyId()));
            existingUser.setCompany(company);
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToDTO(updatedUser);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USER_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_USER_BY_EMAIL, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_USERS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_ACTIVE_USERS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_USER_SEARCH, allEntries = true)
    })
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USER_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_ACTIVE_USERS, allEntries = true)
    })
    public void deactivateUser(Long id) {
        log.debug("Deactivating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USER_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_ACTIVE_USERS, allEntries = true)
    })
    public void activateUser(Long id) {
        log.debug("Activating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(true);
        userRepository.save(user);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.getActive())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User mapToEntity(UserDTO dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    // Paginated methods

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(userPage, userDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getActiveUsers(Pageable pageable) {
        log.debug("Fetching active users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findByActiveTrue(pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(userPage, userDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getInactiveUsers(Pageable pageable) {
        log.debug("Fetching inactive users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findByActiveFalse(pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(userPage, userDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users by keyword: {} with pagination: page={}, size={}", keyword, pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.searchByKeyword(keyword, pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(userPage, userDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getUsersByRole(String roleName, Pageable pageable) {
        log.debug("Fetching users by role: {} with pagination: page={}, size={}", roleName, pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findByRole_RoleName(roleName, pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(userPage, userDTOs);
    }
}
