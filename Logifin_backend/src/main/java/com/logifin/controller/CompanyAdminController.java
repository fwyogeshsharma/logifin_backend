package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.GetCompanyAdminResponse;
import com.logifin.dto.UpdateCompanyAdminRequest;
import com.logifin.service.CompanyAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for Company Admin management APIs.
 * Handles company ownership operations separate from role-based permissions.
 */
@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
@Tag(name = "Company Admin Management", description = "APIs for managing company administrators/owners")
@SecurityRequirement(name = "Bearer Authentication")
public class CompanyAdminController {

    private final CompanyAdminService companyAdminService;

    /**
     * Get company admin for a given company.
     * Returns the admin user details for the specified company.
     */
    @Operation(
            summary = "Get Company Admin",
            description = "Retrieve the admin/owner for a specific company. Returns admin user details including name, email, and phone."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company admin found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company or admin not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{companyId}/admin")
    public ResponseEntity<ApiResponse<GetCompanyAdminResponse>> getCompanyAdmin(
            @Parameter(description = "Company ID") @PathVariable Long companyId) {
        GetCompanyAdminResponse response = companyAdminService.getCompanyAdmin(companyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update/change the company admin.
     * Replaces the existing admin with a new user from the same company.
     */
    @Operation(
            summary = "Update Company Admin",
            description = "Change the company admin to a different user. The new admin must belong to the same company. Requires ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company admin updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - user does not belong to company",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company or user not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/update-admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateCompanyAdmin(
            @Valid @RequestBody UpdateCompanyAdminRequest request) {
        companyAdminService.updateCompanyAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Company admin updated successfully", null));
    }

    /**
     * Check if a company has an admin assigned.
     */
    @Operation(
            summary = "Check Company Admin Exists",
            description = "Check if a company has an admin/owner assigned."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Check completed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{companyId}/admin/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasCompanyAdmin(
            @Parameter(description = "Company ID") @PathVariable Long companyId) {
        boolean hasAdmin = companyAdminService.hasCompanyAdmin(companyId);
        return ResponseEntity.ok(ApiResponse.success(hasAdmin));
    }
}
