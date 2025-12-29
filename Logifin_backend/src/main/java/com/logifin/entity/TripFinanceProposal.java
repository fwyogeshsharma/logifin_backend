package com.logifin.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing a lender's interest to finance a trip.
 * References the contract between lender and transporter for all financial terms.
 * Multiple lenders can show interest in the same trip.
 * One contract can be used for multiple trips.
 */
@Entity
@Table(name = "trip_finance_proposals",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_trip_lender_contract",
                         columnNames = {"trip_id", "lender_id", "contract_id"})
    },
    indexes = {
        @Index(name = "idx_tfp_trip_id", columnList = "trip_id"),
        @Index(name = "idx_tfp_lender_id", columnList = "lender_id"),
        @Index(name = "idx_tfp_contract_id", columnList = "contract_id"),
        @Index(name = "idx_tfp_status", columnList = "status"),
        @Index(name = "idx_tfp_trip_status", columnList = "trip_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripFinanceProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @NotNull(message = "Lender is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @NotNull(message = "Contract is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column(name = "proposed_at", nullable = false)
    @Builder.Default
    private LocalDateTime proposedAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Proposal status enum
     */
    public enum ProposalStatus {
        PENDING,    // Waiting for transporter's decision
        ACCEPTED,   // Transporter accepted this proposal
        REJECTED,   // Transporter rejected this proposal
        WITHDRAWN   // Lender withdrew this proposal
    }
}
