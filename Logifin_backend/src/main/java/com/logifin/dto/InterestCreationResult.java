package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Result of creating interest for a single trip in batch operation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Result of marking interest for a single trip")
public class InterestCreationResult {

    @Schema(description = "Trip ID", example = "101")
    private Long tripId;

    @Schema(description = "Trip number for reference", example = "TRP-101")
    private String tripNumber;

    @Schema(description = "Whether interest was successfully marked", example = "true")
    private boolean success;

    @Schema(description = "Success or error message", example = "Interest marked successfully")
    private String message;

    @Schema(description = "ID of created interest (only if success = true)", example = "201")
    private Long interestId;
}
