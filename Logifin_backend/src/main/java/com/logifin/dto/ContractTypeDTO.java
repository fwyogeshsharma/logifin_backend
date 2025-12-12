package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO for Contract Type master data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Contract type information")
public class ContractTypeDTO {

    @Schema(description = "Contract type ID", example = "1")
    private Long id;

    @Schema(description = "Type name", example = "SINGLE_PARTY_WITH_LOGIFIN")
    private String typeName;

    @Schema(description = "Type description", example = "Single party contract with Logifin")
    private String description;

    @Schema(description = "Number of parties involved", example = "1")
    private Integer partyCount;
}
