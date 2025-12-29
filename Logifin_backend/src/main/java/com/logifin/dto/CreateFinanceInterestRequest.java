package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for lender to mark interest in financing multiple trips.
 * Lender ID comes from authentication context.
 * Contract is automatically determined based on lender, transporter, and consigner.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for lender to express interest in financing multiple trips")
public class CreateFinanceInterestRequest {

    @NotEmpty(message = "At least one trip ID is required")
    @Size(min = 1, max = 100, message = "You can select between 1 and 100 trips at a time")
    @Schema(
        description = "List of trip IDs that lender wants to finance. " +
                     "System will automatically find the contract between lender, transporter, and consigner for each trip.",
        example = "[101, 102, 103, 104]",
        required = true
    )
    private List<Long> tripIds;
}
