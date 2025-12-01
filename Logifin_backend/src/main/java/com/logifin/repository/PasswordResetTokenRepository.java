package com.logifin.repository;

import com.logifin.entity.PasswordResetToken;
import com.logifin.entity.PasswordResetToken.TokenStatus;
import com.logifin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndStatus(String token, TokenStatus status);

    List<PasswordResetToken> findByUserAndStatus(User user, TokenStatus status);

    List<PasswordResetToken> findByUser(User user);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.status = 'ACTIVE' AND t.expiryDate > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.status = 'EXPIRED' WHERE t.status = 'ACTIVE' AND t.expiryDate < :now")
    int expireOldTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.status = 'EXPIRED' WHERE t.user = :user AND t.status = 'ACTIVE'")
    int invalidateUserTokens(@Param("user") User user);

    boolean existsByTokenAndStatus(String token, TokenStatus status);

    @Query("SELECT COUNT(t) > 0 FROM PasswordResetToken t WHERE t.user = :user AND t.status = 'ACTIVE' AND t.createdAt > :since")
    boolean hasRecentToken(@Param("user") User user, @Param("since") LocalDateTime since);
}
