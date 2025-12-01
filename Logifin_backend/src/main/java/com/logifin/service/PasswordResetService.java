package com.logifin.service;

import com.logifin.dto.ForgotPasswordRequest;
import com.logifin.dto.ResetPasswordRequest;

public interface PasswordResetService {

    void processForgotPassword(ForgotPasswordRequest request);

    boolean validateToken(String token);

    void resetPassword(ResetPasswordRequest request);
}
