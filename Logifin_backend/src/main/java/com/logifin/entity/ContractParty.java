package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entity representing a party involved in a contract
 * Junction table linking contracts with users (parties)
 */
@Entity
@Table(name = "contract_parties",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_contract_user", columnNames = {"contract_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_contract_parties_contract", columnList = "contract_id"),
        @Index(name = "idx_contract_parties_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractParty extends BaseEntity {

    @NotNull(message = "Contract is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractParty)) return false;
        ContractParty that = (ContractParty) o;
        return contract != null && contract.equals(that.getContract()) &&
               user != null && user.equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
