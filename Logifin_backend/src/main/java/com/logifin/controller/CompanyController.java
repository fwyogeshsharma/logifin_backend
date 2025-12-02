package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.CompanyDTO;
import com.logifin.dto.PagedResponse;
import com.logifin.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company Management", description = "APIs for managing companies/organizations")
@SecurityRequirement(name = "Bearer Authentication")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(
            summary = "Create Company",
            description = "Create a new company. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Company created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Company with same email/GST/PAN already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(@Valid @RequestBody CompanyDTO companyDTO) {
        CompanyDTO createdCompany = companyService.createCompany(companyDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created successfully", createdCompany));
    }

    @Operation(
            summary = "Get Company by ID",
            description = "Retrieve a company by its ID. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO>> getCompanyById(
            @Parameter(description = "Company ID") @PathVariable Long id) {
        CompanyDTO company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @Operation(
            summary = "Get Company by Email",
            description = "Retrieve a company by its email address. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<CompanyDTO>> getCompanyByEmail(
            @Parameter(description = "Company email") @PathVariable String email) {
        CompanyDTO company = companyService.getCompanyByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @Operation(
            summary = "Get Company by GST Number",
            description = "Retrieve a company by its GST number. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/gst/{gstNumber}")
    public ResponseEntity<ApiResponse<CompanyDTO>> getCompanyByGstNumber(
            @Parameter(description = "GST Number") @PathVariable String gstNumber) {
        CompanyDTO company = companyService.getCompanyByGstNumber(gstNumber);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @Operation(
            summary = "Get All Companies",
            description = "Retrieve all companies. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getAllCompanies() {
        List<CompanyDTO> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Active Companies",
            description = "Retrieve all active companies. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Active companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getActiveCompanies() {
        List<CompanyDTO> companies = companyService.getActiveCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Verified Companies",
            description = "Retrieve all verified companies. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Verified companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/verified")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getVerifiedCompanies() {
        List<CompanyDTO> companies = companyService.getVerifiedCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Pending Verification Companies",
            description = "Retrieve all companies pending verification. Requires ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending verification companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/pending-verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getPendingVerificationCompanies() {
        List<CompanyDTO> companies = companyService.getPendingVerificationCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Update Company",
            description = "Update an existing company. Requires ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDTO>> updateCompany(
            @Parameter(description = "Company ID") @PathVariable Long id,
            @Valid @RequestBody CompanyDTO companyDTO) {
        CompanyDTO updatedCompany = companyService.updateCompany(id, companyDTO);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", updatedCompany));
    }

    @Operation(
            summary = "Delete Company",
            description = "Permanently delete a company. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(
            @Parameter(description = "Company ID") @PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted successfully", null));
    }

    @Operation(
            summary = "Activate Company",
            description = "Activate a company. Requires ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company activated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateCompany(
            @Parameter(description = "Company ID") @PathVariable Long id) {
        companyService.activateCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company activated successfully", null));
    }

    @Operation(
            summary = "Deactivate Company",
            description = "Deactivate a company. Requires ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company deactivated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateCompany(
            @Parameter(description = "Company ID") @PathVariable Long id) {
        companyService.deactivateCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deactivated successfully", null));
    }

    @Operation(
            summary = "Verify Company",
            description = "Verify a company. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company verified successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyCompany(
            @Parameter(description = "Company ID") @PathVariable Long id,
            @Parameter(description = "User ID who is verifying") @RequestParam Long verifiedByUserId) {
        companyService.verifyCompany(id, verifiedByUserId);
        return ResponseEntity.ok(ApiResponse.success("Company verified successfully", null));
    }

    // Paginated endpoints

    @Operation(
            summary = "Get All Companies (Paginated)",
            description = "Retrieve all companies with pagination support. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> getAllCompaniesPaged(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CompanyDTO> companies = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Active Companies (Paginated)",
            description = "Retrieve all active companies with pagination support. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Active companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/active/paged")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> getActiveCompaniesPaged(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CompanyDTO> companies = companyService.getActiveCompanies(pageable);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Verified Companies (Paginated)",
            description = "Retrieve all verified companies with pagination support. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Verified companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/verified/paged")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> getVerifiedCompaniesPaged(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CompanyDTO> companies = companyService.getVerifiedCompanies(pageable);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Search Companies (Paginated)",
            description = "Search companies by keyword with pagination support. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/search/paged")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> searchCompaniesPaged(
            @Parameter(description = "Keyword to search for") @RequestParam String keyword,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CompanyDTO> companies = companyService.searchCompanies(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Companies by City (Paginated)",
            description = "Retrieve companies by city with pagination support. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/city/{city}/paged")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> getCompaniesByCityPaged(
            @Parameter(description = "City name") @PathVariable String city,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CompanyDTO> companies = companyService.getCompaniesByCity(city, pageable);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @Operation(
            summary = "Get Companies by State (Paginated)",
            description = "Retrieve companies by state with pagination support. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Companies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/state/{state}/paged")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> getCompaniesByStatePaged(
            @Parameter(description = "State name") @PathVariable String state,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CompanyDTO> companies = companyService.getCompaniesByState(state, pageable);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }
}
