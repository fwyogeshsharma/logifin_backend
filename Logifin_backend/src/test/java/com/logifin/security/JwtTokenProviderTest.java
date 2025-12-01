package com.logifin.security;

import com.logifin.entity.Role;
import com.logifin.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "testSecretKeyForJwtTokenGenerationInTestEnvironment123456789");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
        jwtTokenProvider.init();

        Role role = Role.builder()
                .roleName("ROLE_USER")
                .build();
        role.setId(1L);

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("password")
                .active(true)
                .role(role)
                .build();
        user.setId(1L);

        userPrincipal = UserPrincipal.create(user);
        authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidJwtToken() {
        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenProvider.generateToken(authentication);

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        String token = jwtTokenProvider.generateToken(authentication);

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("john.doe@test.com");
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate malformed token")
    void shouldInvalidateMalformedToken() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate empty token")
    void shouldInvalidateEmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate null token")
    void shouldInvalidateNullToken() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }
}
