package com.logifin.dto;

import com.logifin.entity.TripFinanceProposal.ProposalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for transporters viewing finance interests in their trips.
 * Shows lender details and contract financial terms.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Finance interest details for transporter view")
public class FinanceInterestForTransporterDTO {

    // Interest Details
    @Schema(description = "Interest ID", example = "1")
    private Long id;

    @Schema(description = "Current status", example = "PENDING")
    private ProposalStatus status;

    @Schema(description = "When lender marked interest", example = "2025-12-19T10:30:00")
    private LocalDateTime interestedAt;

    @Schema(description = "When transporter responded", example = "2025-12-19T15:30:00")
    private LocalDateTime respondedAt;

    // Lender Information
    @Schema(description = "Lender user ID", example = "10")
    private Long lenderId;

    @Schema(description = "Lender name", example = "John Lender")
    private String lenderName;

    @Schema(description = "Lender company name", example = "XYZ Finance")
    private String lenderCompanyName;

    // Contract Financial Terms (from contract)
    @Schema(description = "Contract ID", example = "45")
    private Long contractId;

    @Schema(description = "Interest rate percentage", example = "12.50")
    private BigDecimal interestRate;

    @Schema(description = "Maturity days", example = "30")
    private Integer maturityDays;

    @Schema(description = "Contract expiry date", example = "2026-12-31")
    private LocalDate contractExpiryDate;

    @Schema(description = "Loan percentage of trip amount", example = "80.00")
    private BigDecimal loanPercent;

    @Schema(description = "Loan to value ratio", example = "75.00")
    private BigDecimal ltv;

    @Schema(description = "Penalty ratio", example = "2.00")
    private BigDecimal penaltyRatio;
}
