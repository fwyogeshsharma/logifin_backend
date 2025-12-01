package com.logifin.service;

import com.logifin.config.EmailConfig;
import com.logifin.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailConfig emailConfig;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        lenient().when(emailConfig.getFromAddress()).thenReturn("noreply@logifin.com");
        lenient().when(emailConfig.getFromName()).thenReturn("Logifin");
        lenient().when(emailConfig.getResetLinkBaseUrl()).thenReturn("http://localhost:3000");
        lenient().when(emailConfig.getTokenExpirationMinutes()).thenReturn(30);
    }

    @Nested
    @DisplayName("Send Password Reset Email Tests")
    class SendPasswordResetEmailTests {

        @Test
        @DisplayName("Should send password reset email when enabled")
        void shouldSendPasswordResetEmailWhenEnabled() throws Exception {
            when(emailConfig.isEnabled()).thenReturn(true);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            emailService.sendPasswordResetEmail("john.doe@test.com", "John Doe", "test-token");

            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should not send email when disabled")
        void shouldNotSendEmailWhenDisabled() {
            when(emailConfig.isEnabled()).thenReturn(false);

            emailService.sendPasswordResetEmail("john.doe@test.com", "John Doe", "test-token");

            verify(mailSender, never()).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should log reset link when email disabled")
        void shouldLogResetLinkWhenEmailDisabled() {
            when(emailConfig.isEnabled()).thenReturn(false);

            // Should complete without exception
            emailService.sendPasswordResetEmail("john.doe@test.com", "John Doe", "test-token");

            // Verify mail sender was never called
            verifyNoInteractions(mailSender);
        }
    }

    @Nested
    @DisplayName("Send Password Reset Confirmation Email Tests")
    class SendPasswordResetConfirmationEmailTests {

        @Test
        @DisplayName("Should send confirmation email when enabled")
        void shouldSendConfirmationEmailWhenEnabled() throws Exception {
            when(emailConfig.isEnabled()).thenReturn(true);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            emailService.sendPasswordResetConfirmationEmail("john.doe@test.com", "John Doe");

            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should not send confirmation email when disabled")
        void shouldNotSendConfirmationEmailWhenDisabled() {
            when(emailConfig.isEnabled()).thenReturn(false);

            emailService.sendPasswordResetConfirmationEmail("john.doe@test.com", "John Doe");

            verify(mailSender, never()).send(any(MimeMessage.class));
        }
    }
}
