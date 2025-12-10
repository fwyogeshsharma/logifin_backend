package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for Document Type operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Document type details")
public class DocumentTypeDTO {

    @Schema(description = "Document type ID", example = "1")
    private Long id;

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    @Schema(description = "Unique code for the document type", example = "EWAY_BILL", required = true)
    private String code;

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    @Schema(description = "Human readable display name", example = "E-Way Bill", required = true)
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Description of the document type", example = "Electronic Way Bill for goods transportation")
    private String description;

    @Schema(description = "Whether this document type is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Sort order for display purposes", example = "1")
    private Integer sortOrder;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Request DTO for creating a new document type
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request payload for creating a document type")
    public static class CreateRequest {

        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must not exceed 30 characters")
        @Schema(description = "Unique code for the document type", example = "CUSTOMS_DOC", required = true)
        private String code;

        @NotBlank(message = "Display name is required")
        @Size(max = 100, message = "Display name must not exceed 100 characters")
        @Schema(description = "Human readable display name", example = "Customs Document", required = true)
        private String displayName;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Description of the document type", example = "Customs clearance document")
        private String description;

        @Schema(description = "Sort order for display purposes", example = "6")
        private Integer sortOrder;
    }

    /**
     * Request DTO for updating a document type
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request payload for updating a document type")
    public static class UpdateRequest {

        @Size(max = 100, message = "Display name must not exceed 100 characters")
        @Schema(description = "Human readable display name", example = "E-Way Bill Updated")
        private String displayName;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Description of the document type", example = "Updated description")
        private String description;

        @Schema(description = "Whether this document type is active", example = "true")
        private Boolean isActive;

        @Schema(description = "Sort order for display purposes", example = "1")
        private Integer sortOrder;
    }
}
