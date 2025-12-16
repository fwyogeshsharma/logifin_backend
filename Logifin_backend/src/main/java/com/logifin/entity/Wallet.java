package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_user", columnList = "user_id", unique = true),
    @Index(name = "idx_wallet_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "INR";

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
