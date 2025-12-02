package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {

    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @Size(max = 150, message = "Display name must not exceed 150 characters")
    private String displayName;

    private String logoBase64;

    private String description;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Pincode must not exceed 20 characters")
    private String pincode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "GST number must not exceed 20 characters")
    private String gstNumber;

    @Size(max = 20, message = "PAN number must not exceed 20 characters")
    private String panNumber;

    @Size(max = 50, message = "Company registration number must not exceed 50 characters")
    private String companyRegistrationNumber;

    private Boolean isActive;

    private Boolean isVerified;

    private LocalDateTime verifiedAt;

    private Long verifiedById;

    private String verifiedByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
