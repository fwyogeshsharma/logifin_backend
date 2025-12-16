package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualCreditRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String paymentMethod;

    private String referenceNumber;

    private String remarks;

    private LocalDateTime actualTransferDate;

    private String proofImageBase64;

    private String proofImageFileName;

    private String proofImageMimeType;
}
