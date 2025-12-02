package com.logifin.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for GET /company/{companyId}/admin API.
 * Contains company admin information including the admin user details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCompanyAdminResponse {

    private Long companyId;
    private String companyName;
    private CompanyAdminUserDTO adminUser;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
