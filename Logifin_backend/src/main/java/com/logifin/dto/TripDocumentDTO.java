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

    @Size(max = 100, message = "Document number must not exceed 100 characters")
    @Schema(description = "Document reference number (e.g., E-way Bill Number, Bilty Number)", example = "EWB123456789")
    private String documentNumber;

    @Schema(description = "Base64 encoded document data")
    private String documentBase64;

    @Schema(description = "Content type/MIME type", example = "application/pdf")
    private String contentType;

    @Schema(description = "File size in bytes", example = "102400")
    private Long fileSize;

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
        private String documentNumber;
        private String contentType;
        private Long fileSize;
        private Long uploadedByUserId;
        private String uploadedByUserName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
