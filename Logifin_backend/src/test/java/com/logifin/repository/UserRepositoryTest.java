package com.logifin.repository;

import com.logifin.entity.Role;
import com.logifin.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        testRole = Role.builder()
                .roleName("ROLE_TEST")
                .description("Test Role")
                .build();
        testRole = roleRepository.save(testRole);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("hashedPassword")
                .phone("1234567890")
                .active(true)
                .role(testRole)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Optional<User> found = userRepository.findByEmail("john.doe@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        boolean exists = userRepository.existsByEmail("john.doe@test.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@test.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find all active users")
    void shouldFindAllActiveUsers() {
        User inactiveUser = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@test.com")
                .password("hashedPassword")
                .active(false)
                .role(testRole)
                .build();
        userRepository.save(inactiveUser);

        List<User> activeUsers = userRepository.findByActiveTrue();

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("john.doe@test.com");
    }

    @Test
    @DisplayName("Should search users by name")
    void shouldSearchUsersByName() {
        User anotherUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@test.com")
                .password("hashedPassword")
                .active(true)
                .role(testRole)
                .build();
        userRepository.save(anotherUser);

        List<User> foundByFirstName = userRepository.searchByName("John");
        List<User> foundByLastName = userRepository.searchByName("Smith");

        assertThat(foundByFirstName).hasSize(1);
        assertThat(foundByFirstName.get(0).getFirstName()).isEqualTo("John");
        assertThat(foundByLastName).hasSize(1);
        assertThat(foundByLastName.get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("Should save user with role")
    void shouldSaveUserWithRole() {
        User newUser = User.builder()
                .firstName("New")
                .lastName("User")
                .email("new.user@test.com")
                .password("hashedPassword")
                .active(true)
                .role(testRole)
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isNotNull();
        assertThat(saved.getRole().getRoleName()).isEqualTo("ROLE_TEST");
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        testUser.setFirstName("UpdatedName");
        User updated = userRepository.save(testUser);

        assertThat(updated.getFirstName()).isEqualTo("UpdatedName");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        userRepository.delete(testUser);

        Optional<User> found = userRepository.findById(testUser.getId());
        assertThat(found).isEmpty();
    }
}
