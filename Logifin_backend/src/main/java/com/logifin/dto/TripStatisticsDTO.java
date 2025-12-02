package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for trip statistics/analytics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Trip statistics and analytics")
public class TripStatisticsDTO {

    @Schema(description = "Total number of trips", example = "1500")
    private long totalTrips;

    @Schema(description = "Number of active trips", example = "500")
    private long activeTrips;

    @Schema(description = "Number of in-transit trips", example = "300")
    private long inTransitTrips;

    @Schema(description = "Number of completed trips", example = "650")
    private long completedTrips;

    @Schema(description = "Number of cancelled trips", example = "50")
    private long cancelledTrips;

    @Schema(description = "Total loan amount across all trips", example = "150000000.00")
    private BigDecimal totalLoanAmount;

    @Schema(description = "Average loan amount per trip", example = "100000.00")
    private BigDecimal averageLoanAmount;

    @Schema(description = "Average interest rate", example = "12.5")
    private BigDecimal averageInterestRate;

    @Schema(description = "Average maturity days", example = "30")
    private Double averageMaturityDays;

    @Schema(description = "Total distance covered (km)", example = "2100000.50")
    private BigDecimal totalDistanceKm;

    @Schema(description = "Total weight transported (kg)", example = "7500000.00")
    private BigDecimal totalWeightKg;

    @Schema(description = "Total interest amount", example = "18750000.00")
    private BigDecimal totalInterestAmount;

    @Schema(description = "Trips created today", example = "25")
    private long tripsCreatedToday;

    @Schema(description = "Trips created this week", example = "150")
    private long tripsCreatedThisWeek;

    @Schema(description = "Trips created this month", example = "500")
    private long tripsCreatedThisMonth;

    @Schema(description = "Trip count by status")
    private Map<String, Long> tripsByStatus;

    @Schema(description = "Top pickup locations with trip counts")
    private Map<String, Long> topPickupLocations;

    @Schema(description = "Top destination locations with trip counts")
    private Map<String, Long> topDestinations;

    @Schema(description = "Top transporters with trip counts")
    private Map<String, Long> topTransporters;

    @Schema(description = "Trip count by load type")
    private Map<String, Long> tripsByLoadType;
}
