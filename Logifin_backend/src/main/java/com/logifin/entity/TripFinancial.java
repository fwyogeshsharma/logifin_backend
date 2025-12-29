package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to track trip financing details including platform fees and original amounts
 * Critical for accurate interest calculation - interest is calculated on original amount,
 * not on the net amount after platform fee deduction
 */
@Entity
@Table(name = "trip_financials", indexes = {
    @Index(name = "idx_trip_financials_trip", columnList = "trip_id"),
    @Index(name = "idx_trip_financials_contract", columnList = "contract_id"),
    @Index(name = "idx_trip_financials_status", columnList = "status"),
    @Index(name = "idx_trip_financials_financing_date", columnList = "financing_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripFinancial extends BaseEntity {

    @NotNull(message = "Trip ID is required")
    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @NotNull(message = "Contract ID is required")
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "financing_transaction_id", columnDefinition = "uuid")
    private UUID financingTransactionId;

    @Column(name = "repayment_transaction_id", columnDefinition = "uuid")
    private UUID repaymentTransactionId;

    // Original amounts (before fees)
    @NotNull(message = "Original principal amount is required")
    @Positive(message = "Original principal amount must be positive")
    @Column(name = "original_principal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalPrincipalAmount;

    @NotNull(message = "Platform fee amount is required")
    @Column(name = "platform_fee_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal platformFeeAmount = BigDecimal.ZERO;

    @NotNull(message = "Net amount to transporter is required")
    @Positive(message = "Net amount to transporter must be positive")
    @Column(name = "net_amount_to_transporter", nullable = false, precision = 19, scale = 4)
    private BigDecimal netAmountToTransporter;

    // Interest tracking
    @NotNull(message = "Interest rate is required")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @NotNull(message = "Financing date is required")
    @Column(name = "financing_date", nullable = false)
    private LocalDateTime financingDate;

    @Column(name = "repayment_date")
    private LocalDateTime repaymentDate;

    @Column(name = "days_used")
    private Integer daysUsed;

    @Column(name = "calculated_interest", precision = 19, scale = 4)
    private BigDecimal calculatedInterest;

    // Repayment tracking
    @Column(name = "total_repayment_amount", precision = 19, scale = 4)
    private BigDecimal totalRepaymentAmount;

    @Column(name = "principal_repaid", precision = 19, scale = 4)
    private BigDecimal principalRepaid;

    @Column(name = "interest_repaid", precision = 19, scale = 4)
    private BigDecimal interestRepaid;

    // Status
    @NotNull(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "FINANCED";

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", insertable = false, updatable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financing_transaction_id", insertable = false, updatable = false)
    private Transaction financingTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_transaction_id", insertable = false, updatable = false)
    private Transaction repaymentTransaction;

    /**
     * Trip financial status enumeration
     */
    public enum TripFinancialStatus {
        FINANCED,   // Money has been transferred to transporter
        REPAID,     // Money has been repaid to lender with interest
        DEFAULTED   // Repayment defaulted
    }
}
