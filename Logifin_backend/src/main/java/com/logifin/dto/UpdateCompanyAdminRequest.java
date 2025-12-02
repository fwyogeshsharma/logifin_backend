package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * Request DTO for POST /company/update-admin API.
 * Used to change the company admin to a different user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompanyAdminRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "New admin user ID is required")
    private Long newAdminUserId;
}
