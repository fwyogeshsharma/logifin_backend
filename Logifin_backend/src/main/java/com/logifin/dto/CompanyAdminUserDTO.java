package com.logifin.dto;

import lombok.*;

/**
 * DTO representing the admin user information in company admin responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminUserDTO {

    private Long userId;
    private String name;
    private String email;
    private String phone;
}
