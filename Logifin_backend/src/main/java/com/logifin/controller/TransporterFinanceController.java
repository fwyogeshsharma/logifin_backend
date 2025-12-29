package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.FinanceInterestForTransporterDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Transporter Finance Operations.
 * Handles viewing and responding to lender interests in transporter's trips.
 */
@RestController
@RequestMapping("/api/v1/transporters")
@RequiredArgsConstructor
@Tag(name = "Transporter Finance", description = "APIs for transporters to view and respond to finance interests")
@SecurityRequirement(name = "Bearer Authentication")
public class TransporterFinanceController {

    private final TripFinanceProposalService financeService;

    @Operation(
            summary = "Get Finance Interests for a Trip",
            description = "View all lenders interested in financing a specific trip. " +
                         "Shows lender details and contract financial terms. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "List of finance interests for the trip",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Trip does not belong to this transporter",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires TRANSPORTER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/trips/{tripId}/finance-interests")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<List<FinanceInterestForTransporterDTO>>> getInterestsForTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId,
            @CurrentUser UserPrincipal currentUser) {

        List<FinanceInterestForTransporterDTO> interests = financeService.getInterestsForMyTrip(
                currentUser.getId(), tripId);

        return ResponseEntity
                .ok(ApiResponse.success("Interests retrieved successfully", interests));
    }

    @Operation(
            summary = "Get All Finance Interests for My Trips",
            description = "View all finance interests across all trips belonging to this transporter. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "List of all finance interests",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires TRANSPORTER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/finance-interests")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<List<FinanceInterestForTransporterDTO>>> getAllInterests(
            @CurrentUser UserPrincipal currentUser) {

        List<FinanceInterestForTransporterDTO> interests = financeService.getAllInterestsForMyTrips(
                currentUser.getId());

        return ResponseEntity
                .ok(ApiResponse.success("All interests retrieved successfully", interests));
    }

    @Operation(
            summary = "Accept Lender's Interest",
            description = "Accept a lender's interest to finance the trip. " +
                         "This will automatically reject all other pending interests for the same trip. " +
                         "Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lender accepted successfully. Other pending interests auto-rejected.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Interest cannot be accepted (not in PENDING status or trip already financed)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Finance interest not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires TRANSPORTER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/finance-interests/{id}/accept")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<Void>> acceptInterest(
            @Parameter(description = "Finance interest ID") @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        financeService.acceptLender(currentUser.getId(), id);

        return ResponseEntity
                .ok(ApiResponse.success("Lender accepted successfully. All other pending interests have been rejected.", null));
    }

    @Operation(
            summary = "Reject Lender's Interest",
            description = "Reject a lender's interest to finance the trip. " +
                         "Only PENDING interests can be rejected. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lender rejected successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Interest cannot be rejected (not in PENDING status)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Finance interest not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires TRANSPORTER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/finance-interests/{id}/reject")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<Void>> rejectInterest(
            @Parameter(description = "Finance interest ID") @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        financeService.rejectLender(currentUser.getId(), id);

        return ResponseEntity
                .ok(ApiResponse.success("Lender rejected successfully", null));
    }
}
