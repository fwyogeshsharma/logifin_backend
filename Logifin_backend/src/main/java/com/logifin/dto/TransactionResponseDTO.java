package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseDTO {
    private UUID transactionId;
    private String transactionType;
    private String status;
    private String description;
    private BigDecimal amount;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime actualTransferDate;
    private List<TransactionEntryDTO> entries;
    private ManualTransferRequestDTO manualRequest;
    private Boolean hasDocuments;
}
