package com.logifin.service.impl;

import com.logifin.config.EmailConfig;
import com.logifin.dto.ForgotPasswordRequest;
import com.logifin.dto.ResetPasswordRequest;
import com.logifin.entity.PasswordResetToken;
import com.logifin.entity.PasswordResetToken.TokenStatus;
import com.logifin.entity.User;
import com.logifin.exception.InvalidTokenException;
import com.logifin.exception.TokenExpiredException;
import com.logifin.repository.PasswordResetTokenRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.EmailService;
import com.logifin.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final EmailConfig emailConfig;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Processing forgot password request for email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            // Don't reveal whether user exists - log but return silently
            log.warn("Forgot password requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();

        if (!user.getActive()) {
            log.warn("Forgot password requested for inactive user: {}", email);
            return;
        }

        // Invalidate any existing active tokens for this user
        int invalidatedCount = tokenRepository.invalidateUserTokens(user);
        if (invalidatedCount > 0) {
            log.info("Invalidated {} existing tokens for user: {}", invalidatedCount, email);
        }

        // Generate new token
        String token = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(emailConfig.getTokenExpirationMinutes());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .status(TokenStatus.ACTIVE)
                .build();

        tokenRepository.save(resetToken);
        log.info("Password reset token created for user: {}", email);

        // Send email asynchronously
        String userName = user.getFirstName() + " " + user.getLastName();
        emailService.sendPasswordResetEmail(email, userName, token);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token.trim());

        if (!tokenOptional.isPresent()) {
            log.warn("Token validation failed: Token not found");
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.getStatus() != TokenStatus.ACTIVE) {
            log.warn("Token validation failed: Token status is {}", resetToken.getStatus());
            return false;
        }

        if (resetToken.isExpired()) {
            log.warn("Token validation failed: Token expired");
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken().trim();
        log.info("Processing password reset request");

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidTokenException("Passwords do not match");
        }

        // Find and validate token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        // Check token status
        if (resetToken.getStatus() == TokenStatus.USED) {
            throw new InvalidTokenException("This password reset link has already been used");
        }

        if (resetToken.getStatus() == TokenStatus.EXPIRED) {
            throw new TokenExpiredException("This password reset link has expired");
        }

        // Check expiration
        if (resetToken.isExpired()) {
            // Mark as expired
            resetToken.setStatus(TokenStatus.EXPIRED);
            tokenRepository.save(resetToken);
            throw new TokenExpiredException("This password reset link has expired");
        }

        // Get user
        User user = resetToken.getUser();

        // Update password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Mark token as used
        resetToken.setStatus(TokenStatus.USED);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());

        // Send confirmation email
        String userName = user.getFirstName() + " " + user.getLastName();
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), userName);
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
