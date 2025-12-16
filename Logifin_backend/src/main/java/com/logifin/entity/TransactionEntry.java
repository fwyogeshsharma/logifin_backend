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
@Table(name = "transaction_entries", indexes = {
    @Index(name = "idx_entry_wallet_created", columnList = "wallet_id, created_at DESC"),
    @Index(name = "idx_entry_transaction", columnList = "transaction_id"),
    @Index(name = "idx_entry_wallet_balance", columnList = "wallet_id, id DESC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_transaction_wallet_sequence",
                      columnNames = {"transaction_id", "wallet_id", "entry_sequence"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntry extends BaseEntity {

    @NotNull(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, columnDefinition = "uuid")
    private UUID transactionId;

    @NotNull(message = "Wallet ID is required")
    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @NotBlank(message = "Entry type is required")
    @Column(name = "entry_type", nullable = false, length = 10)
    private String entryType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "entry_sequence", nullable = false)
    @Builder.Default
    private Short entrySequence = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    private Wallet wallet;
}
