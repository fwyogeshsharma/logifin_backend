package com.logifin.dto;

import lombok.*;

/**
 * Response DTO for authentication operations (login/register).
 * Contains user information and JWT token.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long companyId;
    private String companyName;
    private Boolean isCompanyAdmin;

    public AuthResponse(String accessToken, Long userId, String email,
                       String firstName, String lastName, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}
