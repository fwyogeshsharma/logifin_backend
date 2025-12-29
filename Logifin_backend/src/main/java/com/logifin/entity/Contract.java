package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a contract agreement
 * Main table storing contract details including financial terms and document
 */
@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contract_number", columnList = "contract_number", unique = true),
    @Index(name = "idx_contract_type", columnList = "contract_type_id"),
    @Index(name = "idx_contract_manager", columnList = "contract_manager_id"),
    @Index(name = "idx_consigner_company", columnList = "consigner_company_id"),
    @Index(name = "idx_contract_loan_stage", columnList = "loan_stage_id"),
    @Index(name = "idx_contract_status", columnList = "status"),
    @Index(name = "idx_contract_expiry_date", columnList = "expiry_date"),
    @Index(name = "idx_contract_created_at", columnList = "created_at"),
    @Index(name = "idx_contract_created_by", columnList = "created_by_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends BaseEntity {

    // Contract Document stored as base16 (hexadecimal) encoded string
    @NotBlank(message = "Contract document is required")
    @Column(name = "contract_document", nullable = false, columnDefinition = "TEXT")
    @Lob
    private String contractDocument;

    @Size(max = 255, message = "Document name must not exceed 255 characters")
    @Column(name = "contract_document_name", length = 255)
    private String contractDocumentName;

    @Size(max = 100, message = "Content type must not exceed 100 characters")
    @Column(name = "contract_document_content_type", length = 100)
    private String contractDocumentContentType;

    // Financial Terms
    @NotNull(message = "Loan percent is required")
    @DecimalMin(value = "0.00", message = "Loan percent must be at least 0")
    @DecimalMax(value = "100.00", message = "Loan percent must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Loan percent must have at most 3 integer digits and 2 decimal places")
    @Column(name = "loan_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal loanPercent;

    @NotNull(message = "LTV is required")
    @DecimalMin(value = "0.00", message = "LTV must be at least 0")
    @DecimalMax(value = "100.00", message = "LTV must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "LTV must have at most 3 integer digits and 2 decimal places")
    @Column(name = "ltv", nullable = false, precision = 5, scale = 2)
    private BigDecimal ltv;

    @NotNull(message = "Penalty ratio is required")
    @DecimalMin(value = "0.00", message = "Penalty ratio must be at least 0")
    @DecimalMax(value = "100.00", message = "Penalty ratio must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Penalty ratio must have at most 3 integer digits and 2 decimal places")
    @Column(name = "penalty_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal penaltyRatio;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.00", message = "Interest rate must be at least 0")
    @DecimalMax(value = "100.00", message = "Interest rate must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    @NotNull(message = "Maturity days is required")
    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Column(name = "maturity_days", nullable = false)
    @Builder.Default
    private Integer maturityDays = 30;

    // Contract Metadata
    @Size(max = 50, message = "Contract number must not exceed 50 characters")
    @Column(name = "contract_number", unique = true, length = 50)
    private String contractNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    // Foreign Key Relationships
    @NotNull(message = "Contract type is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract_type_id", nullable = false)
    private ContractType contractType;

    @NotNull(message = "Contract manager is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_manager_id", nullable = false)
    private User contractManager;

    @NotNull(message = "Consigner company is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consigner_company_id", nullable = false)
    private Company consignerCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_stage_id")
    private LoanStage loanStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // Status
    @Size(max = 30, message = "Status must not exceed 30 characters")
    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    // One-to-Many relationship with contract parties
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ContractParty> contractParties = new HashSet<>();

    // Utility methods for managing bidirectional relationship
    public void addContractParty(ContractParty party) {
        contractParties.add(party);
        party.setContract(this);
    }

    public void removeContractParty(ContractParty party) {
        contractParties.remove(party);
        party.setContract(null);
    }
}
