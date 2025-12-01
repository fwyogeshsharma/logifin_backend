package com.logifin.service.impl;

import com.logifin.config.EmailConfig;
import com.logifin.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        if (!emailConfig.isEnabled()) {
            log.info("Email sending is disabled. Password reset link for {}: {}/reset-password?token={}",
                    toEmail, emailConfig.getResetLinkBaseUrl(), resetToken);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromName());
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Logifin");

            String resetLink = emailConfig.getResetLinkBaseUrl() + "/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetEmailTemplate(userName, resetLink, emailConfig.getTokenExpirationMinutes());

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetConfirmationEmail(String toEmail, String userName) {
        if (!emailConfig.isEnabled()) {
            log.info("Email sending is disabled. Password reset confirmation for: {}", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromName());
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Successful - Logifin");

            String htmlContent = buildPasswordResetConfirmationTemplate(userName);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset confirmation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset confirmation email to: {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error while sending password reset confirmation email to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailTemplate(String userName, String resetLink, int expirationMinutes) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Password Reset</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\" style=\"padding: 40px 0;\">\n" +
                "                <table role=\"presentation\" style=\"width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">\n" +
                "                    <!-- Header -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 40px 20px 40px; text-align: center; background-color: #2563eb; border-radius: 8px 8px 0 0;\">\n" +
                "                            <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;\">Logifin</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <!-- Content -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px;\">\n" +
                "                            <h2 style=\"color: #1f2937; margin: 0 0 20px 0; font-size: 24px;\">Password Reset Request</h2>\n" +
                "                            <p style=\"color: #4b5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;\">Hello " + escapeHtml(userName) + ",</p>\n" +
                "                            <p style=\"color: #4b5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;\">We received a request to reset your password. Click the button below to create a new password:</p>\n" +
                "                            <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse;\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 20px 0;\">\n" +
                "                                        <a href=\"" + resetLink + "\" style=\"display: inline-block; padding: 14px 32px; background-color: #2563eb; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: 600; border-radius: 6px;\">Reset Password</a>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                            <p style=\"color: #4b5563; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;\">This link will expire in <strong>" + expirationMinutes + " minutes</strong>.</p>\n" +
                "                            <p style=\"color: #4b5563; font-size: 14px; line-height: 1.6; margin: 10px 0 0 0;\">If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>\n" +
                "                            <hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;\">\n" +
                "                            <p style=\"color: #9ca3af; font-size: 12px; line-height: 1.6; margin: 0;\">If the button doesn't work, copy and paste this link into your browser:</p>\n" +
                "                            <p style=\"color: #2563eb; font-size: 12px; line-height: 1.6; margin: 5px 0 0 0; word-break: break-all;\">" + resetLink + "</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <!-- Footer -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 20px 40px; text-align: center; background-color: #f9fafb; border-radius: 0 0 8px 8px;\">\n" +
                "                            <p style=\"color: #9ca3af; font-size: 12px; margin: 0;\">&copy; 2024 Logifin. All rights reserved.</p>\n" +
                "                            <p style=\"color: #9ca3af; font-size: 12px; margin: 5px 0 0 0;\">This is an automated message, please do not reply.</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildPasswordResetConfirmationTemplate(String userName) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Password Reset Successful</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\" style=\"padding: 40px 0;\">\n" +
                "                <table role=\"presentation\" style=\"width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">\n" +
                "                    <!-- Header -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 40px 20px 40px; text-align: center; background-color: #059669; border-radius: 8px 8px 0 0;\">\n" +
                "                            <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;\">Logifin</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <!-- Content -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px;\">\n" +
                "                            <div style=\"text-align: center; margin-bottom: 20px;\">\n" +
                "                                <span style=\"display: inline-block; width: 60px; height: 60px; background-color: #d1fae5; border-radius: 50%; line-height: 60px; font-size: 30px;\">&#10003;</span>\n" +
                "                            </div>\n" +
                "                            <h2 style=\"color: #1f2937; margin: 0 0 20px 0; font-size: 24px; text-align: center;\">Password Reset Successful</h2>\n" +
                "                            <p style=\"color: #4b5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;\">Hello " + escapeHtml(userName) + ",</p>\n" +
                "                            <p style=\"color: #4b5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;\">Your password has been successfully reset. You can now log in with your new password.</p>\n" +
                "                            <p style=\"color: #4b5563; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;\">If you did not make this change, please contact our support team immediately.</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <!-- Footer -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 20px 40px; text-align: center; background-color: #f9fafb; border-radius: 0 0 8px 8px;\">\n" +
                "                            <p style=\"color: #9ca3af; font-size: 12px; margin: 0;\">&copy; 2024 Logifin. All rights reserved.</p>\n" +
                "                            <p style=\"color: #9ca3af; font-size: 12px; margin: 5px 0 0 0;\">This is an automated message, please do not reply.</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
