package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Bid on a Trip in the logistics financing system.
 * Lenders can place bids on trips, and transporters can accept/reject/counter.
 */
@Entity
@Table(name = "trip_bids", indexes = {
    @Index(name = "idx_trip_bids_trip_id", columnList = "trip_id"),
    @Index(name = "idx_trip_bids_lender_id", columnList = "lender_id"),
    @Index(name = "idx_trip_bids_company_id", columnList = "company_id"),
    @Index(name = "idx_trip_bids_status", columnList = "status"),
    @Index(name = "idx_trip_bids_created_at", columnList = "created_at"),
    @Index(name = "idx_trip_bids_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripBid extends BaseEntity {

    @NotNull(message = "Trip is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @NotNull(message = "Lender is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Bid amount must have at most 10 integer digits and 2 decimal places")
    @Column(name = "bid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal bidAmount;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "INR";

    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Column(name = "maturity_days")
    private Integer maturityDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private BidStatus status = BidStatus.PENDING;

    // Counter offer fields
    @DecimalMin(value = "0.01", message = "Counter amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Counter amount must have at most 10 integer digits and 2 decimal places")
    @Column(name = "counter_amount", precision = 12, scale = 2)
    private BigDecimal counterAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Counter interest rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Counter interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Counter interest rate must have at most 3 integer digits and 2 decimal places")
    @Column(name = "counter_interest_rate", precision = 5, scale = 2)
    private BigDecimal counterInterestRate;

    @Min(value = 1, message = "Counter maturity days must be at least 1")
    @Max(value = 365, message = "Counter maturity days must not exceed 365")
    @Column(name = "counter_maturity_days")
    private Integer counterMaturityDays;

    @Column(name = "counter_notes", columnDefinition = "TEXT")
    private String counterNotes;

    @Column(name = "countered_at")
    private LocalDateTime counteredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "countered_by")
    private User counteredBy;

    // Response tracking
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy;

    // General fields
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Bid status enumeration
     */
    public enum BidStatus {
        PENDING,    // New bid awaiting response
        ACCEPTED,   // Bid accepted by transporter
        REJECTED,   // Bid rejected by transporter
        CANCELLED,  // Bid cancelled by lender
        EXPIRED,    // Bid auto-expired
        COUNTERED   // Counter offer made by transporter
    }

    /**
     * Check if bid is still active (can be modified or responded to)
     */
    public boolean isActive() {
        return status == BidStatus.PENDING || status == BidStatus.COUNTERED;
    }

    /**
     * Check if bid has expired based on expires_at
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
