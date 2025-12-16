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
public class TransactionEntryDTO {
    private Long entryId;
    private UUID transactionId;
    private Long walletId;
    private Long userId;
    private String entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Short entrySequence;
    private LocalDateTime createdAt;
}
