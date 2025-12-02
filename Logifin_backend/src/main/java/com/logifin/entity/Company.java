package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_company_name", columnList = "name"),
    @Index(name = "idx_company_email", columnList = "email"),
    @Index(name = "idx_company_gst", columnList = "gst_number"),
    @Index(name = "idx_company_pan", columnList = "pan_number"),
    @Index(name = "idx_company_is_active", columnList = "is_active"),
    @Index(name = "idx_company_is_verified", columnList = "is_verified")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 150, message = "Display name must not exceed 150 characters")
    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "logo_base64", columnDefinition = "TEXT")
    private String logoBase64;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    @Column(name = "website", length = 255)
    private String website;

    @Email(message = "Email should be valid")
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 20, message = "Pincode must not exceed 20 characters")
    @Column(name = "pincode", length = 20)
    private String pincode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "India";

    @Size(max = 20, message = "GST number must not exceed 20 characters")
    @Column(name = "gst_number", unique = true, length = 20)
    private String gstNumber;

    @Size(max = 20, message = "PAN number must not exceed 20 characters")
    @Column(name = "pan_number", unique = true, length = 20)
    private String panNumber;

    @Size(max = 50, message = "Company registration number must not exceed 50 characters")
    @Column(name = "company_registration_number", unique = true, length = 50)
    private String companyRegistrationNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;
}
