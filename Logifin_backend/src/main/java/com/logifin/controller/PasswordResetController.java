package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.ForgotPasswordRequest;
import com.logifin.dto.ResetPasswordRequest;
import com.logifin.dto.ValidateTokenRequest;
import com.logifin.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "APIs for password reset functionality")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Forgot Password",
            description = "Request a password reset link. An email with reset instructions will be sent if the email exists in the system."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Request processed (always returns success for security)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid email format",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.processForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "If the email exists in our system, you will receive a password reset link shortly",
                null));
    }

    @Operation(
            summary = "Validate Reset Token",
            description = "Check if a password reset token is valid and not expired."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateResetToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        boolean isValid = passwordResetService.validateToken(request.getToken());
        Map<String, Boolean> result = Collections.singletonMap("valid", isValid);
        return ResponseEntity.ok(ApiResponse.success(
                isValid ? "Token is valid" : "Token is invalid or expired",
                result));
    }

    @Operation(
            summary = "Reset Password",
            description = "Reset password using a valid token. Token must be active and not expired."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset successful",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token, or passwords don't match",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password has been reset successfully",
                null));
    }
}
