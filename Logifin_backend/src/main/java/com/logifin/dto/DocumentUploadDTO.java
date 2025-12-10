package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for uploading a document with base64 encoded data.
 * Used for all document types: EWAY_BILL, BILTY, ADVANCE_INVOICE, POD, TRUCK_INVOICE
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Document upload payload with document type, number and base64 encoded data")
public class DocumentUploadDTO {

    @NotNull(message = "Document type ID is required")
    @Schema(description = "Document type ID from document_types table", example = "1", required = true)
    private Long documentTypeId;

    @Size(max = 100, message = "Document number must not exceed 100 characters")
    @Schema(description = "Document reference number (e.g., E-way Bill Number, Bilty Number)", example = "EWB123456789")
    private String documentNumber;

    @Schema(description = "Base64 encoded document data (with or without data URI prefix)",
            example = "data:image/png;base64,iVBORw0KGgo...")
    private String documentBase64;

    /**
     * Check if this document has data to upload
     */
    public boolean hasData() {
        return documentBase64 != null && !documentBase64.trim().isEmpty();
    }

    /**
     * Check if this document has any content (data or number)
     */
    public boolean hasContent() {
        return hasData() || (documentNumber != null && !documentNumber.trim().isEmpty());
    }
}
