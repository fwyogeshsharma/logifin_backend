package com.logifin.dto;

import com.logifin.entity.Trip;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for trip search/filter criteria.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Search criteria for filtering trips")
public class TripSearchCriteria {

    @Schema(description = "Search by E-way Bill Number (partial match)", example = "EWB123")
    private String ewayBillNumber;

    @Schema(description = "Search by pickup location (partial match)", example = "Mumbai")
    private String pickup;

    @Schema(description = "Search by destination (partial match)", example = "Delhi")
    private String destination;

    @Schema(description = "Search by sender name (partial match)", example = "ABC")
    private String sender;

    @Schema(description = "Search by receiver name (partial match)", example = "XYZ")
    private String receiver;

    @Schema(description = "Search by transporter name (partial match)", example = "Fast Logistics")
    private String transporter;

    @Schema(description = "Filter by trip status", example = "ACTIVE")
    private Trip.TripStatus status;

    @Schema(description = "Minimum loan amount", example = "50000")
    private BigDecimal minLoanAmount;

    @Schema(description = "Maximum loan amount", example = "200000")
    private BigDecimal maxLoanAmount;

    @Schema(description = "Minimum interest rate", example = "5.0")
    private BigDecimal minInterestRate;

    @Schema(description = "Maximum interest rate", example = "15.0")
    private BigDecimal maxInterestRate;

    @Schema(description = "Minimum maturity days", example = "15")
    private Integer minMaturityDays;

    @Schema(description = "Maximum maturity days", example = "90")
    private Integer maxMaturityDays;

    @Schema(description = "Filter by load type", example = "Electronics")
    private String loadType;

    @Schema(description = "Start date for created_at filter (inclusive)", example = "2024-01-01")
    private LocalDate createdFrom;

    @Schema(description = "End date for created_at filter (inclusive)", example = "2024-12-31")
    private LocalDate createdTo;

    @Schema(description = "Filter by company ID", example = "1")
    private Long companyId;

    @Schema(description = "Filter by created by user ID", example = "1")
    private Long createdByUserId;

    @Schema(description = "General keyword search across multiple fields", example = "Mumbai")
    private String keyword;
}
