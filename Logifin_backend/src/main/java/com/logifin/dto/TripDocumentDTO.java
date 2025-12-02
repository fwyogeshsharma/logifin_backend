package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for Trip Document operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Trip document details")
public class TripDocumentDTO {

    @Schema(description = "Document ID", example = "1")
    private Long id;

    @Schema(description = "Trip ID", example = "1")
    private Long tripId;

    @Schema(description = "Document type ID", example = "1")
    private Long documentTypeId;

    @NotBlank(message = "Document type code is required")
    @Schema(description = "Document type code", example = "EWAY_BILL", required = true)
    private String documentTypeCode;

    @Size(max = 255, message = "Document name must not exceed 255 characters")
    @Schema(description = "Document name", example = "eway_bill_123.pdf")
    private String documentName;

    @Schema(description = "Base64 encoded document data")
    private String documentBase64;

    @Schema(description = "Content type/MIME type", example = "application/pdf")
    private String contentType;

    @Schema(description = "File size in bytes", example = "102400")
    private Long fileSize;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Document description", example = "E-Way Bill for trip EWB123456789")
    private String description;

    @Schema(description = "Uploaded by user ID", example = "1")
    private Long uploadedByUserId;

    @Schema(description = "Uploaded by user name", example = "John Doe")
    private String uploadedByUserName;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Whether document data is present", example = "true")
    private Boolean hasData;

    /**
     * Response-only DTO without the actual document data
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Trip document metadata (without actual data)")
    public static class TripDocumentMetadataDTO {
        private Long id;
        private Long tripId;
        private Long documentTypeId;
        private String documentTypeCode;
        private String documentTypeDisplayName;
        private String documentName;
        private String contentType;
        private Long fileSize;
        private String description;
        private Long uploadedByUserId;
        private String uploadedByUserName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
