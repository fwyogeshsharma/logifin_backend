package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancingTransferRequest {

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "From user ID (contract wallet) is required")
    private Long fromUserId;

    @NotNull(message = "To user ID (transporter) is required")
    private Long toUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    private String remarks;

    private LocalDateTime actualTransferDate;

    private String proofImageBase64;

    private String proofImageFileName;

    private String proofImageMimeType;
}
