package com.logifin.security;

import com.logifin.entity.Role;
import com.logifin.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserPrincipal Tests")
class UserPrincipalTest {

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .roleName("ROLE_ADMIN")
                .description("Admin Role")
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
    }

    @Test
    @DisplayName("Should create UserPrincipal from User entity")
    void shouldCreateUserPrincipalFromUser() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getFirstName()).isEqualTo("John");
        assertThat(userPrincipal.getLastName()).isEqualTo("Doe");
        assertThat(userPrincipal.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(userPrincipal.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    @DisplayName("Should return username as email")
    void shouldReturnUsernameAsEmail() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getUsername()).isEqualTo("john.doe@test.com");
    }

    @Test
    @DisplayName("Should have correct authority from role")
    void shouldHaveCorrectAuthorityFromRole() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getAuthorities()).hasSize(1);
        assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should have empty authorities when no role")
    void shouldHaveEmptyAuthoritiesWhenNoRole() {
        testUser.setRole(null);
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should be enabled when user is active")
    void shouldBeEnabledWhenUserIsActive() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should be disabled when user is inactive")
    void shouldBeDisabledWhenUserIsInactive() {
        testUser.setActive(false);
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return true for account non-expired")
    void shouldReturnTrueForAccountNonExpired() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("Should return true for account non-locked")
    void shouldReturnTrueForAccountNonLocked() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("Should return true for credentials non-expired")
    void shouldReturnTrueForCredentialsNonExpired() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
    }
}
