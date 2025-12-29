package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for updating an existing Contract
 * All fields are optional - only provided fields will be updated
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for updating an existing contract (partial update supported)")
public class UpdateContractRequest {

    // ==================== Contract Document ====================

    @Schema(description = "Contract document encoded as base16 (hexadecimal) string")
    private String contractDocument;

    @Size(max = 255, message = "Document name must not exceed 255 characters")
    @Schema(description = "Name of the contract document file", example = "contract_agreement_v2.pdf")
    private String contractDocumentName;

    @Size(max = 100, message = "Content type must not exceed 100 characters")
    @Schema(description = "MIME type of the document", example = "application/pdf")
    private String contractDocumentContentType;

    // ==================== Financial Terms ====================

    @DecimalMin(value = "0.00", message = "Loan percent must be at least 0")
    @DecimalMax(value = "100.00", message = "Loan percent must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Loan percent must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Loan percentage (0-100)", example = "75.50")
    private BigDecimal loanPercent;

    @DecimalMin(value = "0.00", message = "LTV must be at least 0")
    @DecimalMax(value = "100.00", message = "LTV must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "LTV must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Loan-to-value ratio (0-100)", example = "80.00")
    private BigDecimal ltv;

    @DecimalMin(value = "0.00", message = "Penalty ratio must be at least 0")
    @DecimalMax(value = "100.00", message = "Penalty ratio must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Penalty ratio must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Penalty ratio percentage (0-100)", example = "5.00")
    private BigDecimal penaltyRatio;

    @DecimalMin(value = "0.00", message = "Interest rate must be at least 0")
    @DecimalMax(value = "100.00", message = "Interest rate must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Annual interest rate percentage (0-100)", example = "12.50")
    private BigDecimal interestRate;

    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Schema(description = "Number of days until maturity (1-365)", example = "30")
    private Integer maturityDays;

    // ==================== Contract Metadata ====================

    @Size(max = 50, message = "Contract number must not exceed 50 characters")
    @Schema(description = "Unique contract reference number", example = "CONT-2024-001")
    private String contractNumber;

    @Future(message = "Expiry date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Contract expiration date (ISO format)", example = "2025-12-31")
    private LocalDate expiryDate;

    // ==================== Foreign Keys ====================

    @Schema(description = "Contract type ID", example = "2")
    private Long contractTypeId;

    @Schema(description = "User ID of the contract manager", example = "10")
    private Long contractManagerId;

    @Schema(description = "Company ID of the consigner", example = "5")
    private Long consignerCompanyId;

    @Schema(description = "Loan stage ID (set to null to clear)", example = "2")
    private Long loanStageId;

    // ==================== Contract Parties ====================

    @Valid
    @Schema(description = "List of parties involved in the contract (replaces existing parties)")
    private List<ContractPartyDTO> contractParties;

    // ==================== Status ====================

    @Size(max = 30, message = "Status must not exceed 30 characters")
    @Pattern(regexp = "ACTIVE|EXPIRED|TERMINATED|COMPLETED", message = "Status must be one of: ACTIVE, EXPIRED, TERMINATED, COMPLETED")
    @Schema(description = "Contract status", example = "ACTIVE", allowableValues = {"ACTIVE", "EXPIRED", "TERMINATED", "COMPLETED"})
    private String status;
}
