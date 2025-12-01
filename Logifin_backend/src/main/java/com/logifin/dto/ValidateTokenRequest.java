package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTokenRequest {

    @NotBlank(message = "Token is required")
    private String token;
}
