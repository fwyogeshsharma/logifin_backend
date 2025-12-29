package com.logifin.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LenderAnalyticsDTO {

    private Long lenderId;
    private String lenderName;
    private String lenderEmail;

    // Wallet Information
    private BigDecimal walletBalance; // Current balance in trust account/contract account
    private BigDecimal investedAmount; // Amount currently financed to transporters (earning profit)
    private BigDecimal totalProfit; // Total profit earned (interest)

    // Trip Statistics
    private Integer totalTripsFinanced;
    private Integer activeTrips; // Trips still in progress
    private Integer completedTrips; // Trips completed and repaid

    // Financial Summary
    private BigDecimal totalAmountLent; // Total amount lent across all trips
    private BigDecimal totalAmountReceived; // Total amount received back (principal + interest)
    private BigDecimal pendingRepayments; // Amount yet to be repaid

    // Detailed Investments List
    private List<LenderInvestmentDTO> investments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LenderInvestmentDTO {
        private Long tripId;
        private Long contractId;
        private String tripDetails; // e.g., "Mumbai to Delhi"
        private String transporterName;
        private BigDecimal principalAmount;
        private BigDecimal interestRate; // Annual interest rate
        private Integer daysUsed; // Days for which money was lent
        private BigDecimal interestEarned;
        private BigDecimal totalRepaid; // Principal + Interest
        private BigDecimal portalFeeDeducted;
        private BigDecimal netProfit; // Interest - Portal Fee
        private String status; // ACTIVE, REPAID, PENDING
        private java.time.LocalDateTime financedDate;
        private java.time.LocalDateTime repaymentDate;
    }
}
