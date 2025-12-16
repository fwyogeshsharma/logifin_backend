package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletBalanceDTO {
    private Long walletId;
    private Long userId;
    private String currencyCode;
    private BigDecimal availableBalance;
    private String status;
    private LocalDateTime asOfTime;
}
