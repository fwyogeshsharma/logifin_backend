package com.logifin.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransporterAnalyticsDTO {

    private Long transporterId;
    private String transporterName;
    private String transporterEmail;

    // Wallet Information
    private BigDecimal walletBalance; // Current balance (should be 0 for now)
    private BigDecimal totalBorrowed; // Total amount borrowed from lenders
    private BigDecimal totalRepaid; // Total amount repaid to lenders
    private BigDecimal pendingRepayment; // Amount yet to be repaid

    // Trip Statistics
    private Integer totalTrips;
    private Integer activeTrips; // Trips in progress
    private Integer completedTrips; // Trips completed

    // Financial Summary
    private BigDecimal totalRevenueFromShippers; // Total amount received from shippers
    private BigDecimal totalInterestPaid; // Total interest paid to lenders
    private BigDecimal totalProfit; // Revenue - (Principal + Interest + Expenses)

    // Detailed Borrowings List
    private List<TransporterBorrowingDTO> borrowings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransporterBorrowingDTO {
        private Long tripId;
        private Long contractId;
        private String tripDetails; // e.g., "Mumbai to Delhi"
        private String lenderName;
        private BigDecimal principalAmount;
        private BigDecimal interestRate; // Annual interest rate
        private Integer daysUsed; // Days for which money was borrowed
        private BigDecimal interestPaid;
        private BigDecimal totalRepaid; // Principal + Interest
        private BigDecimal revenueFromShipper; // Amount received from shipper
        private BigDecimal profit; // Revenue - Total Repaid
        private String status; // BORROWED, REPAID, PENDING
        private java.time.LocalDateTime borrowedDate;
        private java.time.LocalDateTime repaymentDate;
    }
}
