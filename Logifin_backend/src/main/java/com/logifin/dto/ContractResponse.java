package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Contract data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response payload containing contract details")
public class ContractResponse {

    @Schema(description = "Contract ID", example = "1")
    private Long id;

    // ==================== Contract Document ====================

    @Schema(description = "Contract document encoded as base16 (hexadecimal) string")
    private String contractDocument;

    @Schema(description = "Name of the contract document file", example = "contract_agreement.pdf")
    private String contractDocumentName;

    @Schema(description = "MIME type of the document", example = "application/pdf")
    private String contractDocumentContentType;

    // ==================== Financial Terms ====================

    @Schema(description = "Loan percentage", example = "75.50")
    private BigDecimal loanPercent;

    @Schema(description = "Loan-to-value ratio", example = "80.00")
    private BigDecimal ltv;

    @Schema(description = "Penalty ratio percentage", example = "5.00")
    private BigDecimal penaltyRatio;

    // ==================== Contract Metadata ====================

    @Schema(description = "Unique contract reference number", example = "CONT-2024-001")
    private String contractNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Contract expiration date", example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Contract status", example = "ACTIVE")
    private String status;

    // ==================== Contract Type ====================

    @Schema(description = "Contract type ID", example = "2")
    private Long contractTypeId;

    @Schema(description = "Contract type name", example = "TWO_PARTY_WITH_LOGIFIN")
    private String contractTypeName;

    @Schema(description = "Number of parties in the contract", example = "2")
    private Integer partyCount;

    // ==================== Contract Manager ====================

    @Schema(description = "Contract manager user ID", example = "10")
    private Long contractManagerId;

    @Schema(description = "Contract manager name", example = "Jane Smith")
    private String contractManagerName;

    @Schema(description = "Contract manager email", example = "jane.smith@logifin.com")
    private String contractManagerEmail;

    // ==================== Consigner Company ====================

    @Schema(description = "Consigner company ID", example = "5")
    private Long consignerCompanyId;

    @Schema(description = "Consigner company name", example = "ABC Transport Ltd")
    private String consignerCompanyName;

    @Schema(description = "Consigner company GST number", example = "27AABCU9603R1ZM")
    private String consignerCompanyGst;

    // ==================== Loan Stage ====================

    @Schema(description = "Loan stage ID", example = "2")
    private Long loanStageId;

    @Schema(description = "Loan stage name", example = "BILTY_UPLOADED")
    private String loanStageName;

    @Schema(description = "Loan stage order", example = "2")
    private Integer loanStageOrder;

    // ==================== Contract Parties ====================

    @Schema(description = "List of parties involved in the contract")
    private List<ContractPartyDTO> contractParties;

    // ==================== Audit Fields ====================

    @Schema(description = "Created by user ID", example = "10")
    private Long createdByUserId;

    @Schema(description = "Created by user name", example = "Jane Smith")
    private String createdByUserName;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Long version;
}
