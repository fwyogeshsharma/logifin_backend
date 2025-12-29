package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Response DTO for batch creation of finance interests.
 * Shows summary and individual results for each trip.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response for batch finance interest creation")
public class BatchFinanceInterestResponse {

    @Schema(description = "Total number of trips requested", example = "4")
    private int totalRequested;

    @Schema(description = "Number of successfully created interests", example = "3")
    private int successCount;

    @Schema(description = "Number of failed attempts", example = "1")
    private int failureCount;

    @Schema(description = "Detailed results for each trip")
    private List<InterestCreationResult> results;
}
