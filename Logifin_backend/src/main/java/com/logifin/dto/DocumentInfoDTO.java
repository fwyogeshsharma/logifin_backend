package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for document metadata information (without the actual document data).
 * Used in trip responses to show what documents are attached.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Document metadata information")
public class DocumentInfoDTO {

    @Schema(description = "Document ID", example = "1")
    private Long id;

    @Schema(description = "Document type ID", example = "1")
    private Long documentTypeId;

    @Schema(description = "Document type code", example = "EWAY_BILL")
    private String documentTypeCode;

    @Schema(description = "Document type display name", example = "E-Way Bill")
    private String documentTypeName;

    @Schema(description = "Document reference number", example = "EWB123456789")
    private String documentNumber;

    @Schema(description = "Whether document has file data", example = "true")
    private Boolean hasData;

    @Schema(description = "Content type/MIME type", example = "image/png")
    private String contentType;

    @Schema(description = "File size in bytes", example = "102400")
    private Long fileSize;

    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadedAt;

    @Schema(description = "Uploaded by user ID")
    private Long uploadedByUserId;

    @Schema(description = "Uploaded by user name")
    private String uploadedByUserName;
}
