package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for trip bid statistics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Statistics for trip bids")
public class TripBidStatisticsDTO {

    @Schema(description = "Total number of bids", example = "100")
    private Long totalBids;

    @Schema(description = "Number of pending bids", example = "25")
    private Long pendingBids;

    @Schema(description = "Number of accepted bids", example = "50")
    private Long acceptedBids;

    @Schema(description = "Number of rejected bids", example = "15")
    private Long rejectedBids;

    @Schema(description = "Number of cancelled bids", example = "5")
    private Long cancelledBids;

    @Schema(description = "Number of expired bids", example = "3")
    private Long expiredBids;

    @Schema(description = "Number of countered bids", example = "2")
    private Long counteredBids;

    @Schema(description = "Total bid amount across all bids", example = "10000000.00")
    private BigDecimal totalBidAmount;

    @Schema(description = "Total accepted bid amount", example = "5000000.00")
    private BigDecimal totalAcceptedAmount;

    @Schema(description = "Average bid amount", example = "100000.00")
    private BigDecimal averageBidAmount;

    @Schema(description = "Average interest rate", example = "12.50")
    private BigDecimal averageInterestRate;

    @Schema(description = "Average maturity days", example = "30")
    private Double averageMaturityDays;

    @Schema(description = "Acceptance rate percentage", example = "50.00")
    private BigDecimal acceptanceRate;

    @Schema(description = "Number of unique lenders", example = "20")
    private Long uniqueLenders;

    @Schema(description = "Number of trips with bids", example = "45")
    private Long tripsWithBids;
}
