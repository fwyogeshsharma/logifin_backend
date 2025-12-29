package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentTransferRequest {

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "From user ID (shipper/contract wallet) is required")
    private Long fromUserId;

    @NotNull(message = "To user ID (lender) is required")
    private Long toUserId;

    // Principal amount is automatically retrieved from TripFinancial record
    // This ensures interest is calculated on the ORIGINAL amount (before platform fee)

    // Interest amount is optional - if not provided, it will be auto-calculated
    // based on the original principal amount and days used
    @DecimalMin(value = "0.00", message = "Interest amount must be 0 or greater")
    private BigDecimal interestAmount;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    private String remarks;

    private LocalDateTime actualTransferDate;

    private String proofImageBase64;

    private String proofImageFileName;

    private String proofImageMimeType;

    // Total amount will be calculated automatically from TripFinancial record
}
