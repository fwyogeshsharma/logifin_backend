package com.logifin.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_created_at", columnList = "created_at DESC"),
    @Index(name = "idx_transaction_type_status", columnList = "transaction_type, status"),
    @Index(name = "idx_transaction_creator", columnList = "created_by_user_id")
})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;

    @NotBlank(message = "Transaction type is required")
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private String metadata;

    @NotNull(message = "Created by user ID is required")
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "actual_transfer_date")
    private LocalDateTime actualTransferDate;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "transaction_purpose", length = 50)
    private String transactionPurpose;

    @Column(name = "gross_amount", precision = 19, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "platform_fee_amount", precision = 19, scale = 4)
    private BigDecimal platformFeeAmount;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", insertable = false, updatable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    /**
     * Transaction purpose enumeration
     */
    public enum TransactionPurpose {
        FINANCING,
        REPAYMENT,
        PORTAL_FEE,
        INTEREST_PAYMENT,
        PRINCIPAL_PAYMENT,
        OTHER
    }
}
