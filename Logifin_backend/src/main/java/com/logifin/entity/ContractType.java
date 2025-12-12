package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * Entity representing contract types
 * Master data table defining types of contracts based on party count
 */
@Entity
@Table(name = "contract_types", indexes = {
    @Index(name = "idx_contract_type_name", columnList = "type_name", unique = true),
    @Index(name = "idx_contract_type_party_count", columnList = "party_count")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractType extends BaseEntity {

    @NotBlank(message = "Type name is required")
    @Size(max = 100, message = "Type name must not exceed 100 characters")
    @Column(name = "type_name", nullable = false, unique = true, length = 100)
    private String typeName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @NotNull(message = "Party count is required")
    @Min(value = 1, message = "Party count must be at least 1")
    @Max(value = 5, message = "Party count must not exceed 5")
    @Column(name = "party_count", nullable = false)
    private Integer partyCount;
}
