package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.entity.TripFinanceProposal.ProposalStatus;
import com.logifin.security.CurrentUser;
import com.logifin.security.UserPrincipal;
import com.logifin.service.TripFinanceProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for Lender Finance Operations.
 * Handles lender's interest in financing trips.
 */
@RestController
@RequestMapping("/api/v1/lenders")
@RequiredArgsConstructor
@Tag(name = "Lender Finance", description = "APIs for lenders to express interest in financing trips")
@SecurityRequirement(name = "Bearer Authentication")
public class LenderFinanceController {

    private final TripFinanceProposalService financeService;

    @Operation(
            summary = "Mark Interest in Multiple Trips (Batch)",
            description = "Lender can select multiple trips to finance in a single request. " +
                         "Returns success/failure for each trip. Requires LENDER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Batch processing completed (check individual results for success/failure)",
                    content = @Content(schema = @Schema(implementation = BatchFinanceInterestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires LENDER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/finance-interests")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<BatchFinanceInterestResponse>> markInterest(
            @Valid @RequestBody CreateFinanceInterestRequest request,
            @CurrentUser UserPrincipal currentUser) {

        BatchFinanceInterestResponse response = financeService.markInterestInMultipleTrips(
                currentUser.getId(), request);

        return ResponseEntity
                .ok(ApiResponse.success("Batch processing completed", response));
    }

    @Operation(
            summary = "Get My Finance Interests",
            description = "Get all trips where lender has shown interest. " +
                         "Optionally filter by status (PENDING, ACCEPTED, REJECTED, WITHDRAWN). Requires LENDER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "List of finance interests",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires LENDER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/finance-interests")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<List<FinanceInterestForLenderDTO>>> getMyInterests(
            @Parameter(description = "Filter by status (optional)")
            @RequestParam(required = false) ProposalStatus status,
            @CurrentUser UserPrincipal currentUser) {

        List<FinanceInterestForLenderDTO> interests = financeService.getMyInterests(
                currentUser.getId(), status);

        return ResponseEntity
                .ok(ApiResponse.success("Interests retrieved successfully", interests));
    }

    @Operation(
            summary = "Get Specific Finance Interest",
            description = "Get details of a specific finance interest by ID. Requires LENDER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Finance interest details",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Finance interest not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires LENDER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/finance-interests/{id}")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<FinanceInterestForLenderDTO>> getInterestById(
            @Parameter(description = "Finance interest ID") @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        FinanceInterestForLenderDTO interest = financeService.getMyInterestById(
                currentUser.getId(), id);

        return ResponseEntity
                .ok(ApiResponse.success("Interest retrieved successfully", interest));
    }

    @Operation(
            summary = "Withdraw Finance Interest",
            description = "Withdraw a pending finance interest. Only PENDING interests can be withdrawn. Requires LENDER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Interest withdrawn successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Interest cannot be withdrawn (not in PENDING status)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Finance interest not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires LENDER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/finance-interests/{id}")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<Void>> withdrawInterest(
            @Parameter(description = "Finance interest ID") @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        financeService.withdrawInterest(currentUser.getId(), id);

        return ResponseEntity
                .ok(ApiResponse.success("Interest withdrawn successfully", null));
    }
}
