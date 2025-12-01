package com.logifin.repository;

import com.logifin.entity.PasswordResetToken;
import com.logifin.entity.PasswordResetToken.TokenStatus;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PasswordResetTokenRepository Tests")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role testRole;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
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

        testToken = PasswordResetToken.builder()
                .user(testUser)
                .token("test-token-12345")
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .status(TokenStatus.ACTIVE)
                .build();
        testToken = tokenRepository.save(testToken);
    }

    @Nested
    @DisplayName("Find By Token Tests")
    class FindByTokenTests {

        @Test
        @DisplayName("Should find token by token string")
        void shouldFindTokenByTokenString() {
            Optional<PasswordResetToken> found = tokenRepository.findByToken("test-token-12345");

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getEmail()).isEqualTo("john.doe@test.com");
            assertThat(found.get().getStatus()).isEqualTo(TokenStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should return empty when token not found")
        void shouldReturnEmptyWhenTokenNotFound() {
            Optional<PasswordResetToken> found = tokenRepository.findByToken("non-existent-token");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find token by token and status")
        void shouldFindTokenByTokenAndStatus() {
            Optional<PasswordResetToken> found = tokenRepository.findByTokenAndStatus("test-token-12345", TokenStatus.ACTIVE);

            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(TokenStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should not find token with wrong status")
        void shouldNotFindTokenWithWrongStatus() {
            Optional<PasswordResetToken> found = tokenRepository.findByTokenAndStatus("test-token-12345", TokenStatus.USED);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By User Tests")
    class FindByUserTests {

        @Test
        @DisplayName("Should find tokens by user")
        void shouldFindTokensByUser() {
            List<PasswordResetToken> tokens = tokenRepository.findByUser(testUser);

            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getToken()).isEqualTo("test-token-12345");
        }

        @Test
        @DisplayName("Should find tokens by user and status")
        void shouldFindTokensByUserAndStatus() {
            // Create another token with USED status
            PasswordResetToken usedToken = PasswordResetToken.builder()
                    .user(testUser)
                    .token("used-token-12345")
                    .expiryDate(LocalDateTime.now().plusMinutes(30))
                    .status(TokenStatus.USED)
                    .build();
            tokenRepository.save(usedToken);

            List<PasswordResetToken> activeTokens = tokenRepository.findByUserAndStatus(testUser, TokenStatus.ACTIVE);
            List<PasswordResetToken> usedTokens = tokenRepository.findByUserAndStatus(testUser, TokenStatus.USED);

            assertThat(activeTokens).hasSize(1);
            assertThat(usedTokens).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find Valid Token Tests")
    class FindValidTokenTests {

        @Test
        @DisplayName("Should find valid token")
        void shouldFindValidToken() {
            Optional<PasswordResetToken> found = tokenRepository.findValidToken("test-token-12345", LocalDateTime.now());

            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Should not find expired token")
        void shouldNotFindExpiredToken() {
            // Create an expired token
            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .user(testUser)
                    .token("expired-token-12345")
                    .expiryDate(LocalDateTime.now().minusMinutes(1))
                    .status(TokenStatus.ACTIVE)
                    .build();
            tokenRepository.save(expiredToken);

            Optional<PasswordResetToken> found = tokenRepository.findValidToken("expired-token-12345", LocalDateTime.now());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should not find used token")
        void shouldNotFindUsedToken() {
            testToken.setStatus(TokenStatus.USED);
            tokenRepository.save(testToken);

            Optional<PasswordResetToken> found = tokenRepository.findValidToken("test-token-12345", LocalDateTime.now());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Expire Old Tokens Tests")
    class ExpireOldTokensTests {

        @Test
        @DisplayName("Should expire old tokens")
        void shouldExpireOldTokens() {
            // Create an old expired token
            PasswordResetToken oldToken = PasswordResetToken.builder()
                    .user(testUser)
                    .token("old-token-12345")
                    .expiryDate(LocalDateTime.now().minusMinutes(1))
                    .status(TokenStatus.ACTIVE)
                    .build();
            tokenRepository.save(oldToken);

            int expiredCount = tokenRepository.expireOldTokens(LocalDateTime.now());

            assertThat(expiredCount).isEqualTo(1);

            Optional<PasswordResetToken> found = tokenRepository.findByToken("old-token-12345");
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(TokenStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should not expire valid tokens")
        void shouldNotExpireValidTokens() {
            int expiredCount = tokenRepository.expireOldTokens(LocalDateTime.now());

            assertThat(expiredCount).isEqualTo(0);

            Optional<PasswordResetToken> found = tokenRepository.findByToken("test-token-12345");
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(TokenStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Invalidate User Tokens Tests")
    class InvalidateUserTokensTests {

        @Test
        @DisplayName("Should invalidate all user tokens")
        void shouldInvalidateAllUserTokens() {
            // Create another active token for the same user
            PasswordResetToken anotherToken = PasswordResetToken.builder()
                    .user(testUser)
                    .token("another-token-12345")
                    .expiryDate(LocalDateTime.now().plusMinutes(30))
                    .status(TokenStatus.ACTIVE)
                    .build();
            tokenRepository.save(anotherToken);

            int invalidatedCount = tokenRepository.invalidateUserTokens(testUser);

            assertThat(invalidatedCount).isEqualTo(2);

            List<PasswordResetToken> activeTokens = tokenRepository.findByUserAndStatus(testUser, TokenStatus.ACTIVE);
            assertThat(activeTokens).isEmpty();
        }

        @Test
        @DisplayName("Should not invalidate tokens of other users")
        void shouldNotInvalidateTokensOfOtherUsers() {
            // Create another user with a token
            User anotherUser = User.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("jane.doe@test.com")
                    .password("hashedPassword")
                    .active(true)
                    .role(testRole)
                    .build();
            anotherUser = userRepository.save(anotherUser);

            PasswordResetToken anotherUserToken = PasswordResetToken.builder()
                    .user(anotherUser)
                    .token("jane-token-12345")
                    .expiryDate(LocalDateTime.now().plusMinutes(30))
                    .status(TokenStatus.ACTIVE)
                    .build();
            tokenRepository.save(anotherUserToken);

            tokenRepository.invalidateUserTokens(testUser);

            Optional<PasswordResetToken> janeToken = tokenRepository.findByToken("jane-token-12345");
            assertThat(janeToken).isPresent();
            assertThat(janeToken.get().getStatus()).isEqualTo(TokenStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Exists By Token And Status Tests")
    class ExistsByTokenAndStatusTests {

        @Test
        @DisplayName("Should return true for existing token with status")
        void shouldReturnTrueForExistingTokenWithStatus() {
            boolean exists = tokenRepository.existsByTokenAndStatus("test-token-12345", TokenStatus.ACTIVE);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existing token")
        void shouldReturnFalseForNonExistingToken() {
            boolean exists = tokenRepository.existsByTokenAndStatus("non-existent", TokenStatus.ACTIVE);

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false for existing token with wrong status")
        void shouldReturnFalseForExistingTokenWithWrongStatus() {
            boolean exists = tokenRepository.existsByTokenAndStatus("test-token-12345", TokenStatus.USED);

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Has Recent Token Tests")
    class HasRecentTokenTests {

        @Test
        @DisplayName("Should return true when user has recent token")
        void shouldReturnTrueWhenUserHasRecentToken() {
            boolean hasRecent = tokenRepository.hasRecentToken(testUser, LocalDateTime.now().minusMinutes(5));

            assertThat(hasRecent).isTrue();
        }

        @Test
        @DisplayName("Should return false when no recent token")
        void shouldReturnFalseWhenNoRecentToken() {
            boolean hasRecent = tokenRepository.hasRecentToken(testUser, LocalDateTime.now().plusMinutes(5));

            assertThat(hasRecent).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Entity Tests")
    class TokenEntityTests {

        @Test
        @DisplayName("Should save token successfully")
        void shouldSaveTokenSuccessfully() {
            PasswordResetToken newToken = PasswordResetToken.builder()
                    .user(testUser)
                    .token("new-token-12345")
                    .expiryDate(LocalDateTime.now().plusMinutes(30))
                    .status(TokenStatus.ACTIVE)
                    .build();

            PasswordResetToken saved = tokenRepository.save(newToken);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update token status successfully")
        void shouldUpdateTokenStatusSuccessfully() {
            testToken.setStatus(TokenStatus.USED);
            PasswordResetToken updated = tokenRepository.save(testToken);

            assertThat(updated.getStatus()).isEqualTo(TokenStatus.USED);
        }

        @Test
        @DisplayName("Token isExpired should return true for expired token")
        void tokenIsExpiredShouldReturnTrueForExpiredToken() {
            testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));

            assertThat(testToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Token isExpired should return false for valid token")
        void tokenIsExpiredShouldReturnFalseForValidToken() {
            assertThat(testToken.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Token isValid should check both status and expiry")
        void tokenIsValidShouldCheckBothStatusAndExpiry() {
            assertThat(testToken.isValid()).isTrue();

            testToken.setStatus(TokenStatus.USED);
            assertThat(testToken.isValid()).isFalse();

            testToken.setStatus(TokenStatus.ACTIVE);
            testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            assertThat(testToken.isValid()).isFalse();
        }
    }
}
