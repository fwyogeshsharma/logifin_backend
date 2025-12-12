package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.LoanStageDTO;
import com.logifin.dto.PagedResponse;
import com.logifin.service.LoanStageService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-stages")
@RequiredArgsConstructor
@Tag(name = "Loan Stage Management", description = "APIs for managing loan stages (master data)")
@SecurityRequirement(name = "Bearer Authentication")
public class LoanStageController {

    private final LoanStageService loanStageService;

    @Operation(
            summary = "Create Loan Stage",
            description = "Create a new loan stage. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Loan stage created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Loan stage already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LoanStageDTO>> createLoanStage(@Valid @RequestBody LoanStageDTO loanStageDTO) {
        LoanStageDTO createdLoanStage = loanStageService.createLoanStage(loanStageDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan stage created successfully", createdLoanStage));
    }

    @Operation(
            summary = "Get Loan Stage by ID",
            description = "Retrieve a loan stage by its ID. Requires authentication."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanStageDTO>> getLoanStageById(
            @Parameter(description = "Loan Stage ID") @PathVariable Long id) {
        LoanStageDTO loanStage = loanStageService.getLoanStageById(id);
        return ResponseEntity.ok(ApiResponse.success("Loan stage retrieved successfully", loanStage));
    }

    @Operation(
            summary = "Get Loan Stage by Name",
            description = "Retrieve a loan stage by its stage name. Requires authentication."
    )
    @GetMapping("/by-name/{stageName}")
    public ResponseEntity<ApiResponse<LoanStageDTO>> getLoanStageByStageName(
            @Parameter(description = "Stage Name") @PathVariable String stageName) {
        LoanStageDTO loanStage = loanStageService.getLoanStageByStageName(stageName);
        return ResponseEntity.ok(ApiResponse.success("Loan stage retrieved successfully", loanStage));
    }

    @Operation(
            summary = "Get All Loan Stages",
            description = "Retrieve all loan stages. Requires authentication."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanStageDTO>>> getAllLoanStages() {
        List<LoanStageDTO> loanStages = loanStageService.getAllLoanStages();
        return ResponseEntity.ok(ApiResponse.success("Loan stages retrieved successfully", loanStages));
    }

    @Operation(
            summary = "Get All Loan Stages (Paginated)",
            description = "Retrieve all loan stages with pagination. Requires authentication."
    )
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<LoanStageDTO>>> getAllLoanStagesPaginated(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "stageOrder") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<LoanStageDTO> loanStages = loanStageService.getAllLoanStages(pageable);
        return ResponseEntity.ok(ApiResponse.success("Loan stages retrieved successfully", loanStages));
    }

    @Operation(
            summary = "Update Loan Stage",
            description = "Update an existing loan stage. Requires SUPER_ADMIN role."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LoanStageDTO>> updateLoanStage(
            @Parameter(description = "Loan Stage ID") @PathVariable Long id,
            @Valid @RequestBody LoanStageDTO loanStageDTO) {
        LoanStageDTO updatedLoanStage = loanStageService.updateLoanStage(id, loanStageDTO);
        return ResponseEntity.ok(ApiResponse.success("Loan stage updated successfully", updatedLoanStage));
    }

    @Operation(
            summary = "Delete Loan Stage",
            description = "Delete a loan stage. Requires SUPER_ADMIN role."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLoanStage(
            @Parameter(description = "Loan Stage ID") @PathVariable Long id) {
        loanStageService.deleteLoanStage(id);
        return ResponseEntity.ok(ApiResponse.success("Loan stage deleted successfully", null));
    }
}
