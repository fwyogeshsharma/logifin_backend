package com.logifin.service;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String userName, String resetToken);

    void sendPasswordResetConfirmationEmail(String toEmail, String userName);
}
