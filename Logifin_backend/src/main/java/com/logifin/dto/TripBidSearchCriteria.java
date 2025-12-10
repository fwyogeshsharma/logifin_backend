package com.logifin.dto;

import com.logifin.entity.TripBid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Search criteria for filtering trip bids.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Search criteria for filtering trip bids")
public class TripBidSearchCriteria {

    @Schema(description = "Filter by trip ID", example = "1")
    private Long tripId;

    @Schema(description = "Filter by lender ID", example = "5")
    private Long lenderId;

    @Schema(description = "Filter by company ID", example = "2")
    private Long companyId;

    @Schema(description = "Filter by bid status", example = "PENDING")
    private TripBid.BidStatus status;

    @Schema(description = "Minimum bid amount", example = "50000.00")
    private BigDecimal minBidAmount;

    @Schema(description = "Maximum bid amount", example = "200000.00")
    private BigDecimal maxBidAmount;

    @Schema(description = "Filter bids created from date", example = "2024-01-01")
    private LocalDate createdFrom;

    @Schema(description = "Filter bids created to date", example = "2024-12-31")
    private LocalDate createdTo;

    @Schema(description = "Include expired bids", example = "false")
    private Boolean includeExpired;

    @Schema(description = "Keyword search (searches lender name, company name, notes)", example = "finance")
    private String keyword;
}
