package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for bulk upload operations (CSV/Excel).
 * Contains success and failure information for each row.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response for bulk trip upload operation")
public class BulkUploadResponseDTO {

    @Schema(description = "Total number of rows processed", example = "100")
    private int totalRows;

    @Schema(description = "Number of successfully created trips", example = "95")
    private int successCount;

    @Schema(description = "Number of failed rows", example = "5")
    private int failureCount;

    @Schema(description = "List of successfully created trip IDs")
    @Builder.Default
    private List<Long> successfulTripIds = new ArrayList<>();

    @Schema(description = "List of error details for failed rows")
    @Builder.Default
    private List<ErrorRowDTO> errors = new ArrayList<>();

    @Schema(description = "Upload status message", example = "Bulk upload completed with 95 successes and 5 failures")
    private String message;

    @Schema(description = "Whether the upload was completely successful", example = "false")
    private boolean completeSuccess;

    @Schema(description = "Processing time in milliseconds", example = "1500")
    private long processingTimeMs;

    /**
     * Helper method to add a successful trip ID
     */
    public void addSuccessfulTripId(Long tripId) {
        if (this.successfulTripIds == null) {
            this.successfulTripIds = new ArrayList<>();
        }
        this.successfulTripIds.add(tripId);
        this.successCount = this.successfulTripIds.size();
    }

    /**
     * Helper method to add an error
     */
    public void addError(ErrorRowDTO error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.failureCount = this.errors.size();
    }

    /**
     * Calculate and set the summary message
     */
    public void calculateSummary() {
        this.totalRows = this.successCount + this.failureCount;
        this.completeSuccess = this.failureCount == 0;
        if (this.completeSuccess) {
            this.message = String.format("All %d trips created successfully", this.successCount);
        } else {
            this.message = String.format("Bulk upload completed: %d successful, %d failed out of %d total",
                    this.successCount, this.failureCount, this.totalRows);
        }
    }
}
