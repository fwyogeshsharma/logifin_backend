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
 * Request DTO for creating a new Contract
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new contract")
public class CreateContractRequest {

    // ==================== Contract Document ====================

    @NotBlank(message = "Contract document is required (base16/hex encoded)")
    @Schema(description = "Contract document encoded as base16 (hexadecimal) string", required = true)
    private String contractDocument;

    @Size(max = 255, message = "Document name must not exceed 255 characters")
    @Schema(description = "Name of the contract document file", example = "contract_agreement.pdf")
    private String contractDocumentName;

    @Size(max = 100, message = "Content type must not exceed 100 characters")
    @Schema(description = "MIME type of the document", example = "application/pdf")
    private String contractDocumentContentType;

    // ==================== Financial Terms ====================

    @NotNull(message = "Loan percent is required")
    @DecimalMin(value = "0.00", message = "Loan percent must be at least 0")
    @DecimalMax(value = "100.00", message = "Loan percent must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Loan percent must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Loan percentage (0-100)", example = "75.50", required = true)
    private BigDecimal loanPercent;

    @NotNull(message = "LTV is required")
    @DecimalMin(value = "0.00", message = "LTV must be at least 0")
    @DecimalMax(value = "100.00", message = "LTV must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "LTV must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Loan-to-value ratio (0-100)", example = "80.00", required = true)
    private BigDecimal ltv;

    @NotNull(message = "Penalty ratio is required")
    @DecimalMin(value = "0.00", message = "Penalty ratio must be at least 0")
    @DecimalMax(value = "100.00", message = "Penalty ratio must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Penalty ratio must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Penalty ratio percentage (0-100)", example = "5.00", required = true)
    private BigDecimal penaltyRatio;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.00", message = "Interest rate must be at least 0")
    @DecimalMax(value = "100.00", message = "Interest rate must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Annual interest rate percentage (0-100)", example = "12.50", required = true)
    private BigDecimal interestRate;

    @NotNull(message = "Maturity days is required")
    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Schema(description = "Number of days until maturity (1-365)", example = "30", required = true)
    private Integer maturityDays;

    // ==================== Contract Metadata ====================

    @Size(max = 50, message = "Contract number must not exceed 50 characters")
    @Schema(description = "Unique contract reference number", example = "CONT-2024-001")
    private String contractNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Contract expiration date (ISO format)", example = "2025-12-31", required = true)
    private LocalDate expiryDate;

    // ==================== Foreign Keys ====================

    @NotNull(message = "Contract type ID is required")
    @Schema(description = "Contract type ID", example = "2", required = true)
    private Long contractTypeId;

    @NotNull(message = "Contract manager ID is required")
    @Schema(description = "User ID of the contract manager", example = "10", required = true)
    private Long contractManagerId;

    @NotNull(message = "Consigner company ID is required")
    @Schema(description = "Company ID of the consigner", example = "5", required = true)
    private Long consignerCompanyId;

    @Schema(description = "Loan stage ID (optional)", example = "1")
    private Long loanStageId;

    // ==================== Contract Parties ====================

    @Valid
    @Schema(description = "List of parties involved in the contract")
    private List<ContractPartyDTO> contractParties;

    // ==================== Status ====================

    @Size(max = 30, message = "Status must not exceed 30 characters")
    @Pattern(regexp = "ACTIVE|EXPIRED|TERMINATED|COMPLETED", message = "Status must be one of: ACTIVE, EXPIRED, TERMINATED, COMPLETED")
    @Schema(description = "Contract status", example = "ACTIVE", allowableValues = {"ACTIVE", "EXPIRED", "TERMINATED", "COMPLETED"})
    private String status;
}
