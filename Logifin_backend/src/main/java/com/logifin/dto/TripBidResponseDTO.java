package com.logifin.dto;

import com.logifin.entity.TripBid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Trip Bid data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing trip bid details")
public class TripBidResponseDTO {

    @Schema(description = "Bid ID", example = "1")
    private Long id;

    // Trip info
    @Schema(description = "Trip ID", example = "1")
    private Long tripId;

    @Schema(description = "Trip pickup location", example = "Mumbai, Maharashtra")
    private String tripPickup;

    @Schema(description = "Trip destination", example = "Delhi, NCR")
    private String tripDestination;

    @Schema(description = "Trip loan amount requested", example = "150000.00")
    private BigDecimal tripLoanAmount;

    // Lender info
    @Schema(description = "Lender user ID", example = "5")
    private Long lenderId;

    @Schema(description = "Lender name", example = "John Doe")
    private String lenderName;

    @Schema(description = "Lender email", example = "john@lender.com")
    private String lenderEmail;

    // Company info
    @Schema(description = "Lender company ID", example = "2")
    private Long companyId;

    @Schema(description = "Lender company name", example = "ABC Finance Ltd")
    private String companyName;

    // Bid details
    @Schema(description = "Bid amount", example = "100000.00")
    private BigDecimal bidAmount;

    @Schema(description = "Currency code", example = "INR")
    private String currency;

    @Schema(description = "Proposed interest rate", example = "12.50")
    private BigDecimal interestRate;

    @Schema(description = "Proposed maturity days", example = "30")
    private Integer maturityDays;

    @Schema(description = "Bid status", example = "PENDING")
    private TripBid.BidStatus status;

    @Schema(description = "Additional notes", example = "Flexible on terms")
    private String notes;

    // Counter offer details
    @Schema(description = "Counter offer amount", example = "110000.00")
    private BigDecimal counterAmount;

    @Schema(description = "Counter offer interest rate", example = "10.00")
    private BigDecimal counterInterestRate;

    @Schema(description = "Counter offer maturity days", example = "45")
    private Integer counterMaturityDays;

    @Schema(description = "Counter offer notes", example = "Adjusted terms")
    private String counterNotes;

    @Schema(description = "Counter offer timestamp")
    private LocalDateTime counteredAt;

    @Schema(description = "User who made counter offer ID", example = "3")
    private Long counteredById;

    @Schema(description = "User who made counter offer name", example = "Jane Smith")
    private String counteredByName;

    // Response tracking
    @Schema(description = "Response timestamp (when accepted/rejected)")
    private LocalDateTime respondedAt;

    @Schema(description = "User who responded ID", example = "3")
    private Long respondedById;

    @Schema(description = "User who responded name", example = "Jane Smith")
    private String respondedByName;

    @Schema(description = "Rejection reason if rejected")
    private String rejectionReason;

    // Expiry
    @Schema(description = "Bid expiry timestamp")
    private LocalDateTime expiresAt;

    @Schema(description = "Whether bid has expired", example = "false")
    private Boolean isExpired;

    // Audit fields
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    // Calculated fields
    @Schema(description = "Calculated total interest amount", example = "12500.00")
    private BigDecimal totalInterestAmount;

    @Schema(description = "Calculated total amount (bid + interest)", example = "112500.00")
    private BigDecimal totalAmount;
}
