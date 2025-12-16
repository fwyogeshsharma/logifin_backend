package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManualTransferRequestDTO {
    private Long requestId;
    private UUID transactionId;
    private String requestType;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String paymentMethod;
    private String referenceNumber;
    private String remarks;
    private Long enteredByUserId;
    private LocalDateTime enteredAt;
}
