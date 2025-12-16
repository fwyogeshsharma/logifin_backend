package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletStatementDTO {
    private Long walletId;
    private Long userId;
    private String currencyCode;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private List<TransactionEntryDTO> entries;
    private Integer totalEntries;
}
