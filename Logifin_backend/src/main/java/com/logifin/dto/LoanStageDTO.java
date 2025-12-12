package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO for Loan Stage master data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Loan stage information")
public class LoanStageDTO {

    @Schema(description = "Loan stage ID", example = "1")
    private Long id;

    @Schema(description = "Stage name", example = "PENDING")
    private String stageName;

    @Schema(description = "Stage description", example = "Initial stage - contract created but no documents uploaded")
    private String description;

    @Schema(description = "Sequential order of the stage", example = "1")
    private Integer stageOrder;
}
