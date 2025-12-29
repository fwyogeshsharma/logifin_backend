package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.security.CurrentUser;
import com.logifin.security.UserPrincipal;
import com.logifin.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "APIs for financial analytics and reporting for lenders, transporters, and shippers")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ============== LENDER ANALYTICS ==============

    @GetMapping("/lender/{lenderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'LENDER')")
    @Operation(summary = "Get lender analytics",
               description = "Get comprehensive analytics for a lender including wallet balance, invested amount, " +
                           "profit earned, and all investments with details.")
    public ResponseEntity<ApiResponse<LenderAnalyticsDTO>> getLenderAnalytics(
            @PathVariable Long lenderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        // Users can only access their own analytics unless they are admin
        if (!currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            if (!currentUser.getId().equals(lenderId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You can only access your own analytics"));
            }
        }

        LenderAnalyticsDTO analytics = analyticsService.getLenderAnalytics(lenderId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/lender/{lenderId}/investments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'LENDER')")
    @Operation(summary = "Get lender investments with filters",
               description = "Get paginated list of lender's investments with optional filters for transporter, trip, and status.")
    public ResponseEntity<ApiResponse<Page<LenderAnalyticsDTO.LenderInvestmentDTO>>> getLenderInvestments(
            @PathVariable Long lenderId,
            @RequestParam(required = false) Long transporterId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "financedDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        // Users can only access their own analytics unless they are admin
        if (!currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            if (!currentUser.getId().equals(lenderId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You can only access your own analytics"));
            }
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LenderAnalyticsDTO.LenderInvestmentDTO> investments = analyticsService.getLenderInvestments(
                lenderId, transporterId, tripId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(investments));
    }

    // ============== TRANSPORTER ANALYTICS ==============

    @GetMapping("/transporter/{transporterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TRANSPORTER')")
    @Operation(summary = "Get transporter analytics",
               description = "Get comprehensive analytics for a transporter including wallet balance, borrowed amount, " +
                           "repaid amount, profit earned, and all borrowings with details.")
    public ResponseEntity<ApiResponse<TransporterAnalyticsDTO>> getTransporterAnalytics(
            @PathVariable Long transporterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        // Users can only access their own analytics unless they are admin
        if (!currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            if (!currentUser.getId().equals(transporterId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You can only access your own analytics"));
            }
        }

        TransporterAnalyticsDTO analytics = analyticsService.getTransporterAnalytics(transporterId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/transporter/{transporterId}/borrowings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TRANSPORTER')")
    @Operation(summary = "Get transporter borrowings with filters",
               description = "Get paginated list of transporter's borrowings with optional filters for lender, trip, and status.")
    public ResponseEntity<ApiResponse<Page<TransporterAnalyticsDTO.TransporterBorrowingDTO>>> getTransporterBorrowings(
            @PathVariable Long transporterId,
            @RequestParam(required = false) Long lenderId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "borrowedDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        // Users can only access their own analytics unless they are admin
        if (!currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            if (!currentUser.getId().equals(transporterId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You can only access your own analytics"));
            }
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransporterAnalyticsDTO.TransporterBorrowingDTO> borrowings = analyticsService.getTransporterBorrowings(
                transporterId, lenderId, tripId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(borrowings));
    }

    // ============== SHIPPER ANALYTICS ==============

    @GetMapping("/shipper/{shipperId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SHIPPER')")
    @Operation(summary = "Get shipper analytics",
               description = "Get comprehensive analytics for a shipper including total amount paid, " +
                           "amount pending, and all payments with details.")
    public ResponseEntity<ApiResponse<ShipperAnalyticsDTO>> getShipperAnalytics(
            @PathVariable Long shipperId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        // Users can only access their own analytics unless they are admin
        if (!currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            if (!currentUser.getId().equals(shipperId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You can only access your own analytics"));
            }
        }

        ShipperAnalyticsDTO analytics = analyticsService.getShipperAnalytics(shipperId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/shipper/{shipperId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SHIPPER')")
    @Operation(summary = "Get shipper payments with filters",
               description = "Get paginated list of shipper's payments with optional filters for transporter, trip, and status.")
    public ResponseEntity<ApiResponse<Page<ShipperAnalyticsDTO.ShipperPaymentDTO>>> getShipperPayments(
            @PathVariable Long shipperId,
            @RequestParam(required = false) Long transporterId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "tripCompletedDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        // Users can only access their own analytics unless they are admin
        if (!currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            if (!currentUser.getId().equals(shipperId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You can only access your own analytics"));
            }
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ShipperAnalyticsDTO.ShipperPaymentDTO> payments = analyticsService.getShipperPayments(
                shipperId, transporterId, tripId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
