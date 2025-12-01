package com.logifin.repository;

import com.logifin.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role testRole;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();

        testRole = Role.builder()
                .roleName("ROLE_TEST")
                .description("Test Role")
                .build();
        testRole = roleRepository.save(testRole);
    }

    @Test
    @DisplayName("Should find role by role name")
    void shouldFindRoleByRoleName() {
        Optional<Role> found = roleRepository.findByRoleName("ROLE_TEST");

        assertThat(found).isPresent();
        assertThat(found.get().getRoleName()).isEqualTo("ROLE_TEST");
        assertThat(found.get().getDescription()).isEqualTo("Test Role");
    }

    @Test
    @DisplayName("Should return empty when role name not found")
    void shouldReturnEmptyWhenRoleNameNotFound() {
        Optional<Role> found = roleRepository.findByRoleName("ROLE_NONEXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if role exists by name")
    void shouldCheckIfRoleExistsByName() {
        boolean exists = roleRepository.existsByRoleName("ROLE_TEST");
        boolean notExists = roleRepository.existsByRoleName("ROLE_NONEXISTENT");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should save role successfully")
    void shouldSaveRoleSuccessfully() {
        Role newRole = Role.builder()
                .roleName("ROLE_NEW")
                .description("New Role")
                .build();

        Role saved = roleRepository.save(newRole);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoleName()).isEqualTo("ROLE_NEW");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() {
        roleRepository.delete(testRole);

        Optional<Role> found = roleRepository.findById(testRole.getId());
        assertThat(found).isEmpty();
    }
}
