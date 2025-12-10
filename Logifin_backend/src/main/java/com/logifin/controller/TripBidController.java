package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.entity.TripBid;
import com.logifin.security.CurrentUser;
import com.logifin.security.UserPrincipal;
import com.logifin.service.TripBidService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Trip Bid Management APIs.
 * Provides endpoints for creating, managing, and responding to bids on trips.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Trip Bid Management", description = "APIs for managing bids on trips")
@SecurityRequirement(name = "Bearer Authentication")
public class TripBidController {

    private final TripBidService tripBidService;

    // ==================== Lender Operations ====================

    @Operation(
            summary = "Create Bid",
            description = "Create a new bid on a trip. Requires LENDER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Bid created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Already have active bid on this trip",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires LENDER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/bid")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> createBid(
            @Valid @RequestBody TripBidRequestDTO requestDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO createdBid = tripBidService.createBid(requestDTO, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bid created successfully", createdBid));
    }

    @Operation(
            summary = "Update Bid",
            description = "Update an existing bid. Only PENDING bids can be updated. Requires LENDER role."
    )
    @PutMapping("/bid/{bidId}")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> updateBid(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @Valid @RequestBody TripBidRequestDTO requestDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO updatedBid = tripBidService.updateBid(bidId, requestDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Bid updated successfully", updatedBid));
    }

    @Operation(
            summary = "Cancel Bid",
            description = "Cancel your own bid. Only active bids can be cancelled. Requires LENDER role."
    )
    @DeleteMapping("/bid/{bidId}")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<Void>> cancelBid(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @CurrentUser UserPrincipal currentUser) {
        tripBidService.cancelBid(bidId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Bid cancelled successfully", null));
    }

    @Operation(
            summary = "Accept Counter Offer",
            description = "Accept a counter offer made by the transporter. Requires LENDER role."
    )
    @PostMapping("/bid/{bidId}/accept-counter")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> acceptCounterOffer(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO acceptedBid = tripBidService.acceptCounterOffer(bidId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Counter offer accepted successfully", acceptedBid));
    }

    @Operation(
            summary = "Reject Counter Offer",
            description = "Reject a counter offer made by the transporter. Requires LENDER role."
    )
    @PostMapping("/bid/{bidId}/reject-counter")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> rejectCounterOffer(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO rejectedBid = tripBidService.rejectCounterOffer(bidId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Counter offer rejected successfully", rejectedBid));
    }

    @Operation(
            summary = "Get My Bids",
            description = "Get all bids placed by the current lender. Requires LENDER role."
    )
    @GetMapping("/bids/my")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<PagedResponse<TripBidResponseDTO>>> getMyBids(
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal currentUser) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<TripBidResponseDTO> bids;
        if (status != null && !status.isEmpty()) {
            bids = tripBidService.getBidsByLenderAndStatus(currentUser.getId(), status, pageable);
        } else {
            bids = tripBidService.getBidsByLender(currentUser.getId(), pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @Operation(
            summary = "Get My Bid Statistics",
            description = "Get bid statistics for the current lender. Requires LENDER role."
    )
    @GetMapping("/bids/my/statistics")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<TripBidStatisticsDTO>> getMyBidStatistics(
            @CurrentUser UserPrincipal currentUser) {
        TripBidStatisticsDTO statistics = tripBidService.getBidStatisticsForLender(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    // ==================== Transporter Operations ====================

    @Operation(
            summary = "Accept Bid",
            description = "Accept a bid on your trip. All other bids will be automatically rejected. Requires TRANSPORTER role."
    )
    @PostMapping("/bid/{bidId}/accept")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> acceptBid(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO acceptedBid = tripBidService.acceptBid(bidId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Bid accepted successfully", acceptedBid));
    }

    @Operation(
            summary = "Reject Bid",
            description = "Reject a bid on your trip. Requires TRANSPORTER role."
    )
    @PostMapping("/bid/{bidId}/reject")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> rejectBid(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @RequestBody(required = false) TripBidActionDTO actionDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO rejectedBid = tripBidService.rejectBid(bidId, actionDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Bid rejected successfully", rejectedBid));
    }

    @Operation(
            summary = "Counter Bid",
            description = "Make a counter offer on a bid. Requires TRANSPORTER role."
    )
    @PostMapping("/bid/{bidId}/counter")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> counterBid(
            @Parameter(description = "Bid ID") @PathVariable Long bidId,
            @Valid @RequestBody TripBidCounterOfferDTO counterOfferDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripBidResponseDTO counteredBid = tripBidService.counterBid(bidId, counterOfferDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Counter offer made successfully", counteredBid));
    }

    // ==================== Trip Bid Queries ====================

    @Operation(
            summary = "Get Bids for Trip",
            description = "Get all bids for a specific trip. Available to any authenticated user."
    )
    @GetMapping("/trip/{tripId}/bids")
    public ResponseEntity<ApiResponse<PagedResponse<TripBidResponseDTO>>> getBidsForTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<TripBidResponseDTO> bids = tripBidService.getBidsForTrip(tripId, pageable);
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @Operation(
            summary = "Get Active Bids for Trip",
            description = "Get active (PENDING or COUNTERED) bids for a trip. Available to any authenticated user."
    )
    @GetMapping("/trip/{tripId}/bids/active")
    public ResponseEntity<ApiResponse<List<TripBidResponseDTO>>> getActiveBidsForTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        List<TripBidResponseDTO> bids = tripBidService.getActiveBidsForTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @Operation(
            summary = "Get Accepted Bid for Trip",
            description = "Get the accepted bid for a trip if exists. Available to any authenticated user."
    )
    @GetMapping("/trip/{tripId}/bid/accepted")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> getAcceptedBidForTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        TripBidResponseDTO bid = tripBidService.getAcceptedBidForTrip(tripId);
        if (bid == null) {
            return ResponseEntity.ok(ApiResponse.success("No accepted bid for this trip", null));
        }
        return ResponseEntity.ok(ApiResponse.success(bid));
    }

    @Operation(
            summary = "Get Bid Statistics for Trip",
            description = "Get bid statistics for a specific trip. Available to any authenticated user."
    )
    @GetMapping("/trip/{tripId}/bids/statistics")
    public ResponseEntity<ApiResponse<TripBidStatisticsDTO>> getBidStatisticsForTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        TripBidStatisticsDTO statistics = tripBidService.getBidStatisticsForTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    // ==================== General Bid Queries ====================

    @Operation(
            summary = "Get Bid by ID",
            description = "Get a bid by its ID. Available to any authenticated user."
    )
    @GetMapping("/bid/{bidId}")
    public ResponseEntity<ApiResponse<TripBidResponseDTO>> getBidById(
            @Parameter(description = "Bid ID") @PathVariable Long bidId) {
        TripBidResponseDTO bid = tripBidService.getBidById(bidId);
        return ResponseEntity.ok(ApiResponse.success(bid));
    }

    @Operation(
            summary = "Search Bids",
            description = "Search bids with various filters. Available to any authenticated user."
    )
    @GetMapping("/bids")
    public ResponseEntity<ApiResponse<PagedResponse<TripBidResponseDTO>>> searchBids(
            @Parameter(description = "Trip ID filter") @RequestParam(required = false) Long tripId,
            @Parameter(description = "Lender ID filter") @RequestParam(required = false) Long lenderId,
            @Parameter(description = "Company ID filter") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum bid amount") @RequestParam(required = false) BigDecimal minBidAmount,
            @Parameter(description = "Maximum bid amount") @RequestParam(required = false) BigDecimal maxBidAmount,
            @Parameter(description = "Created from date (yyyy-MM-dd)") @RequestParam(required = false) String createdFrom,
            @Parameter(description = "Created to date (yyyy-MM-dd)") @RequestParam(required = false) String createdTo,
            @Parameter(description = "Include expired bids") @RequestParam(required = false) Boolean includeExpired,
            @Parameter(description = "Keyword search") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        TripBidSearchCriteria criteria = TripBidSearchCriteria.builder()
                .tripId(tripId)
                .lenderId(lenderId)
                .companyId(companyId)
                .status(status != null ? TripBid.BidStatus.valueOf(status.toUpperCase()) : null)
                .minBidAmount(minBidAmount)
                .maxBidAmount(maxBidAmount)
                .createdFrom(createdFrom != null ? LocalDate.parse(createdFrom) : null)
                .createdTo(createdTo != null ? LocalDate.parse(createdTo) : null)
                .includeExpired(includeExpired)
                .keyword(keyword)
                .build();

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<TripBidResponseDTO> bids = tripBidService.searchBids(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    // ==================== Admin/Statistics Operations ====================

    @Operation(
            summary = "Get Overall Bid Statistics",
            description = "Get overall bid statistics. Requires ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/bids/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TripBidStatisticsDTO>> getBidStatistics() {
        TripBidStatisticsDTO statistics = tripBidService.getBidStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @Operation(
            summary = "Check Active Bid on Trip",
            description = "Check if current lender has an active bid on a trip. Requires LENDER role."
    )
    @GetMapping("/trip/{tripId}/bid/check")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<Boolean>> checkActiveBidOnTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId,
            @CurrentUser UserPrincipal currentUser) {
        boolean hasActiveBid = tripBidService.hasActiveBidOnTrip(tripId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(hasActiveBid));
    }

    @Operation(
            summary = "Process Expired Bids",
            description = "Manually trigger processing of expired bids. Requires SUPER_ADMIN role."
    )
    @PostMapping("/bids/process-expired")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> processExpiredBids() {
        int count = tripBidService.processExpiredBids();
        return ResponseEntity.ok(ApiResponse.success("Processed " + count + " expired bids", count));
    }
}
