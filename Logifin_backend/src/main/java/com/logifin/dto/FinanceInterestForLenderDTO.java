package com.logifin.dto;

import com.logifin.entity.TripFinanceProposal.ProposalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for lenders viewing their finance interests.
 * Shows trip details and current status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Finance interest details for lender view")
public class FinanceInterestForLenderDTO {

    // Interest Details
    @Schema(description = "Interest ID", example = "1")
    private Long id;

    @Schema(description = "Current status", example = "PENDING")
    private ProposalStatus status;

    @Schema(description = "When lender marked interest", example = "2025-12-19T10:30:00")
    private LocalDateTime interestedAt;

    @Schema(description = "When transporter responded", example = "2025-12-19T15:30:00")
    private LocalDateTime respondedAt;

    // Trip Information
    @Schema(description = "Trip ID", example = "101")
    private Long tripId;

    @Schema(description = "Trip number", example = "TRP-101")
    private String tripNumber;

    @Schema(description = "Trip pickup location", example = "Mumbai, Maharashtra")
    private String origin;

    @Schema(description = "Trip destination", example = "Delhi, NCR")
    private String destination;

    @Schema(description = "Trip loan amount", example = "50000.00")
    private BigDecimal estimatedAmount;

    @Schema(description = "Transporter user ID", example = "10")
    private Long transporterId;

    @Schema(description = "Transporter name", example = "ABC Transport")
    private String transporterName;

    @Schema(description = "Transporter company name", example = "ABC Logistics Pvt Ltd")
    private String transporterCompanyName;

    // Contract Financial Terms
    @Schema(description = "Contract ID used for this interest", example = "45")
    private Long contractId;

    @Schema(description = "Interest rate from contract", example = "12.50")
    private BigDecimal interestRate;

    @Schema(description = "Maturity days from contract", example = "30")
    private Integer maturityDays;
}
