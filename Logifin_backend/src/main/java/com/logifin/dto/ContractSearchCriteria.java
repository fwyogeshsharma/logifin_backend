package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Search criteria for filtering contracts
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Search criteria for filtering contracts")
public class ContractSearchCriteria {

    @Schema(description = "Filter by contract number (partial match)", example = "CONT-2024")
    private String contractNumber;

    @Schema(description = "Filter by contract type ID", example = "2")
    private Long contractTypeId;

    @Schema(description = "Filter by contract manager ID", example = "10")
    private Long contractManagerId;

    @Schema(description = "Filter by consigner company ID", example = "5")
    private Long consignerCompanyId;

    @Schema(description = "Filter by loan stage ID", example = "2")
    private Long loanStageId;

    @Schema(description = "Filter by status", example = "ACTIVE", allowableValues = {"ACTIVE", "EXPIRED", "TERMINATED", "COMPLETED"})
    private String status;

    // ==================== Financial Range Filters ====================

    @Schema(description = "Minimum loan percent", example = "50.00")
    private BigDecimal minLoanPercent;

    @Schema(description = "Maximum loan percent", example = "90.00")
    private BigDecimal maxLoanPercent;

    @Schema(description = "Minimum LTV", example = "60.00")
    private BigDecimal minLtv;

    @Schema(description = "Maximum LTV", example = "85.00")
    private BigDecimal maxLtv;

    @Schema(description = "Minimum penalty ratio", example = "0.00")
    private BigDecimal minPenaltyRatio;

    @Schema(description = "Maximum penalty ratio", example = "10.00")
    private BigDecimal maxPenaltyRatio;

    // ==================== Date Range Filters ====================

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Filter contracts expiring on or after this date", example = "2024-01-01")
    private LocalDate expiryDateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Filter contracts expiring on or before this date", example = "2025-12-31")
    private LocalDate expiryDateTo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Filter contracts created on or after this date", example = "2024-01-01")
    private LocalDate createdDateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Filter contracts created on or before this date", example = "2024-12-31")
    private LocalDate createdDateTo;

    // ==================== User/Party Filters ====================

    @Schema(description = "Filter by party user ID (contracts where user is a party)", example = "15")
    private Long partyUserId;

    @Schema(description = "Filter by created by user ID", example = "10")
    private Long createdByUserId;

    // ==================== Search Keyword ====================

    @Schema(description = "Keyword search across contract number, company name, manager name", example = "ABC")
    private String keyword;
}
