package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity representing loan stages in the contract workflow
 * Master data table for tracking document upload stages
 */
@Entity
@Table(name = "loan_stages", indexes = {
    @Index(name = "idx_loan_stage_name", columnList = "stage_name", unique = true),
    @Index(name = "idx_loan_stage_order", columnList = "stage_order", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanStage extends BaseEntity {

    @NotBlank(message = "Stage name is required")
    @Size(max = 50, message = "Stage name must not exceed 50 characters")
    @Column(name = "stage_name", nullable = false, unique = true, length = 50)
    private String stageName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @NotNull(message = "Stage order is required")
    @Min(value = 1, message = "Stage order must be positive")
    @Column(name = "stage_order", nullable = false, unique = true)
    private Integer stageOrder;
}
