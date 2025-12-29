package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.service.ContractService;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract Management", description = "APIs for managing contract agreements")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractController {

    private final ContractService contractService;

    // ==================== CRUD Operations ====================

    @Operation(
            summary = "Create Contract",
            description = "Create a new contract agreement. Requires TRUST_ACCOUNT role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Contract created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Requires TRUST_ACCOUNT role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('TRUST_ACCOUNT')")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.logifin.security.UserPrincipal principal) {
        ContractResponse contract = contractService.createContract(request, principal.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contract created successfully", contract));
    }

    @Operation(
            summary = "Get Contract by ID",
            description = "Retrieve a contract by its ID. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contract found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Contract not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        ContractResponse contract = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success("Contract retrieved successfully", contract));
    }

    @Operation(
            summary = "Get Contract by Contract Number",
            description = "Retrieve a contract by its contract number. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contract found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Contract not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/by-number/{contractNumber}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractByNumber(
            @Parameter(description = "Contract Number") @PathVariable String contractNumber) {
        ContractResponse contract = contractService.getContractByContractNumber(contractNumber);
        return ResponseEntity.ok(ApiResponse.success("Contract retrieved successfully", contract));
    }

    @Operation(
            summary = "Get All Contracts",
            description = "Retrieve all contracts with pagination. Requires authentication."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ContractResponse>>> getAllContracts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ContractResponse> contracts = contractService.getAllContracts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved successfully", contracts));
    }

    @Operation(
            summary = "Search Contracts",
            description = "Search contracts with advanced criteria. Requires authentication."
    )
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ContractResponse>>> searchContracts(
            @RequestBody ContractSearchCriteria criteria,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ContractResponse> contracts = contractService.searchContracts(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved successfully", contracts));
    }

    @Operation(
            summary = "Update Contract",
            description = "Update an existing contract. Requires TRUST_ACCOUNT role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contract updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Contract not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Requires TRUST_ACCOUNT role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRUST_ACCOUNT')")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Valid @RequestBody UpdateContractRequest request) {
        ContractResponse contract = contractService.updateContract(id, request);
        return ResponseEntity.ok(ApiResponse.success("Contract updated successfully", contract));
    }

    @Operation(
            summary = "Delete Contract",
            description = "Delete a contract. Requires TRUST_ACCOUNT role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contract deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Contract not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Requires TRUST_ACCOUNT role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRUST_ACCOUNT')")
    public ResponseEntity<ApiResponse<Void>> deleteContract(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success("Contract deleted successfully", null));
    }

    // ==================== Contract Parties Management ====================

    @Operation(
            summary = "Add Party to Contract",
            description = "Add a party to a contract. Requires TRUST_ACCOUNT role."
    )
    @PostMapping("/{contractId}/parties")
    @PreAuthorize("hasRole('TRUST_ACCOUNT')")
    public ResponseEntity<ApiResponse<ContractResponse>> addPartyToContract(
            @Parameter(description = "Contract ID") @PathVariable Long contractId,
            @Valid @RequestBody ContractPartyDTO partyDTO) {
        ContractResponse contract = contractService.addPartyToContract(contractId, partyDTO);
        return ResponseEntity.ok(ApiResponse.success("Party added successfully", contract));
    }

    @Operation(
            summary = "Remove Party from Contract",
            description = "Remove a party from a contract. Requires TRUST_ACCOUNT role."
    )
    @DeleteMapping("/{contractId}/parties/{userId}")
    @PreAuthorize("hasRole('TRUST_ACCOUNT')")
    public ResponseEntity<ApiResponse<ContractResponse>> removePartyFromContract(
            @Parameter(description = "Contract ID") @PathVariable Long contractId,
            @Parameter(description = "User ID") @PathVariable Long userId) {
        ContractResponse contract = contractService.removePartyFromContract(contractId, userId);
        return ResponseEntity.ok(ApiResponse.success("Party removed successfully", contract));
    }

    @Operation(
            summary = "Get Contract Parties",
            description = "Get all parties for a specific contract. Requires authentication."
    )
    @GetMapping("/{contractId}/parties")
    public ResponseEntity<ApiResponse<List<ContractPartyDTO>>> getContractParties(
            @Parameter(description = "Contract ID") @PathVariable Long contractId) {
        List<ContractPartyDTO> parties = contractService.getContractParties(contractId);
        return ResponseEntity.ok(ApiResponse.success("Contract parties retrieved successfully", parties));
    }

    // ==================== Query Endpoints ====================

    @Operation(
            summary = "Get Contracts by Status",
            description = "Retrieve contracts by status. Requires authentication."
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<ContractResponse>>> getContractsByStatus(
            @Parameter(description = "Contract status") @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ContractResponse> contracts = contractService.getContractsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved successfully", contracts));
    }

    @Operation(
            summary = "Get Contracts by Company",
            description = "Retrieve contracts for a specific company. Requires authentication."
    )
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<PagedResponse<ContractResponse>>> getContractsByCompany(
            @Parameter(description = "Company ID") @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ContractResponse> contracts = contractService.getContractsByCompany(companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved successfully", contracts));
    }

    @Operation(
            summary = "Get Contracts by Manager",
            description = "Retrieve contracts for a specific manager. Requires authentication."
    )
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ApiResponse<PagedResponse<ContractResponse>>> getContractsByManager(
            @Parameter(description = "Manager ID") @PathVariable Long managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ContractResponse> contracts = contractService.getContractsByManager(managerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved successfully", contracts));
    }

    @Operation(
            summary = "Get Expiring Contracts",
            description = "Retrieve contracts expiring soon. Requires authentication."
    )
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getExpiringContracts(
            @Parameter(description = "Days ahead") @RequestParam(defaultValue = "30") int daysAhead) {
        List<ContractResponse> contracts = contractService.getContractsExpiringSoon(daysAhead);
        return ResponseEntity.ok(ApiResponse.success("Expiring contracts retrieved successfully", contracts));
    }

    @Operation(
            summary = "Get Expired Contracts",
            description = "Retrieve expired contracts. Requires authentication."
    )
    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getExpiredContracts() {
        List<ContractResponse> contracts = contractService.getExpiredContracts();
        return ResponseEntity.ok(ApiResponse.success("Expired contracts retrieved successfully", contracts));
    }

    // ==================== Statistics Endpoints ====================

    @Operation(
            summary = "Get Contract Count by Type",
            description = "Get statistics on contract count by type. Requires authentication."
    )
    @GetMapping("/stats/count-by-type")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getContractCountByType() {
        Map<String, Long> stats = contractService.getContractCountByType();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @Operation(
            summary = "Get Contract Count by Status",
            description = "Get statistics on contract count by status. Requires authentication."
    )
    @GetMapping("/stats/count-by-status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getContractCountByStatus() {
        Map<String, Long> stats = contractService.getContractCountByStatus();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @Operation(
            summary = "Get Top Contract Managers",
            description = "Get top contract managers by contract count. Requires authentication."
    )
    @GetMapping("/stats/top-managers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopContractManagers(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> stats = contractService.getTopContractManagers(limit);
        return ResponseEntity.ok(ApiResponse.success("Top managers retrieved successfully", stats));
    }

    @Operation(
            summary = "Get Top Consigner Companies",
            description = "Get top consigner companies by contract count. Requires authentication."
    )
    @GetMapping("/stats/top-companies")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopConsignerCompanies(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> stats = contractService.getTopConsignerCompanies(limit);
        return ResponseEntity.ok(ApiResponse.success("Top companies retrieved successfully", stats));
    }
}
