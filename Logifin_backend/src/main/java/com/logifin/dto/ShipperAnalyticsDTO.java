package com.logifin.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperAnalyticsDTO {

    private Long shipperId;
    private String shipperName;
    private String shipperEmail;

    // Financial Summary
    private BigDecimal totalAmountPaid; // Total amount paid for trips
    private BigDecimal totalAmountPending; // Total amount pending for trips

    // Trip Statistics
    private Integer totalTrips;
    private Integer completedTrips; // Trips completed and paid
    private Integer pendingTrips; // Trips not yet paid

    // Detailed Payments List
    private List<ShipperPaymentDTO> payments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipperPaymentDTO {
        private Long tripId;
        private Long contractId;
        private String tripDetails; // e.g., "Mumbai to Delhi"
        private String transporterName;
        private String invoiceNumber;
        private BigDecimal tripCost; // Cost of the trip
        private BigDecimal amountPaid;
        private BigDecimal amountPending;
        private String status; // PAID, PENDING, PARTIAL
        private java.time.LocalDateTime tripCompletedDate;
        private java.time.LocalDateTime paymentDate;
    }
}
