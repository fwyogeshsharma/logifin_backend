package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "manual_transfer_requests", indexes = {
    @Index(name = "idx_manual_transaction", columnList = "transaction_id", unique = true),
    @Index(name = "idx_manual_from_user", columnList = "from_user_id"),
    @Index(name = "idx_manual_to_user", columnList = "to_user_id"),
    @Index(name = "idx_manual_entered_at", columnList = "entered_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualTransferRequest extends BaseEntity {

    @NotNull(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID transactionId;

    @NotBlank(message = "Request type is required")
    @Column(name = "request_type", nullable = false, length = 20)
    private String requestType;

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "to_user_id")
    private Long toUserId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @NotNull(message = "Entered by user ID is required")
    @Column(name = "entered_by_user_id", nullable = false)
    private Long enteredByUserId;

    @Column(name = "entered_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime enteredAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", insertable = false, updatable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", insertable = false, updatable = false)
    private User toUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by_user_id", insertable = false, updatable = false)
    private User enteredBy;
}
