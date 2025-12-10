package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.DocumentTypeDTO;
import com.logifin.service.DocumentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for Document Type operations.
 */
@RestController
@RequestMapping("/api/v1/document-types")
@RequiredArgsConstructor
@Tag(name = "Document Type Management", description = "APIs for managing document types")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    @Operation(
            summary = "Get All Document Types",
            description = "Retrieve all document types ordered by sort order. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document types retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentTypeDTO>>> getAllDocumentTypes() {
        List<DocumentTypeDTO> documentTypes = documentTypeService.getAllDocumentTypes();
        return ResponseEntity.ok(ApiResponse.success(documentTypes));
    }

    @Operation(
            summary = "Get Active Document Types",
            description = "Retrieve only active document types ordered by sort order. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Active document types retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DocumentTypeDTO>>> getActiveDocumentTypes() {
        List<DocumentTypeDTO> documentTypes = documentTypeService.getActiveDocumentTypes();
        return ResponseEntity.ok(ApiResponse.success(documentTypes));
    }

    @Operation(
            summary = "Get Document Type by ID",
            description = "Retrieve a document type by its ID. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document type found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document type not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentTypeDTO>> getDocumentTypeById(
            @Parameter(description = "Document type ID") @PathVariable Long id) {
        DocumentTypeDTO documentType = documentTypeService.getDocumentTypeById(id);
        return ResponseEntity.ok(ApiResponse.success(documentType));
    }

    @Operation(
            summary = "Get Document Type by Code",
            description = "Retrieve a document type by its unique code. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document type found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document type not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DocumentTypeDTO>> getDocumentTypeByCode(
            @Parameter(description = "Document type code (e.g., EWAY_BILL)") @PathVariable String code) {
        DocumentTypeDTO documentType = documentTypeService.getDocumentTypeByCode(code);
        return ResponseEntity.ok(ApiResponse.success(documentType));
    }

    @Operation(
            summary = "Check if Code Exists",
            description = "Check if a document type code already exists. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Check completed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/exists/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkCodeExists(
            @Parameter(description = "Document type code to check") @PathVariable String code) {
        boolean exists = documentTypeService.codeExists(code);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @Operation(
            summary = "Create Document Type",
            description = "Create a new document type. Requires SUPER_ADMIN or ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Document type created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or code already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentTypeDTO>> createDocumentType(
            @Valid @RequestBody DocumentTypeDTO.CreateRequest request) {
        DocumentTypeDTO createdDocumentType = documentTypeService.createDocumentType(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document type created successfully", createdDocumentType));
    }

    @Operation(
            summary = "Update Document Type",
            description = "Update an existing document type. Requires SUPER_ADMIN or ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document type updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document type not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentTypeDTO>> updateDocumentType(
            @Parameter(description = "Document type ID") @PathVariable Long id,
            @Valid @RequestBody DocumentTypeDTO.UpdateRequest request) {
        DocumentTypeDTO updatedDocumentType = documentTypeService.updateDocumentType(id, request);
        return ResponseEntity.ok(ApiResponse.success("Document type updated successfully", updatedDocumentType));
    }

    @Operation(
            summary = "Delete Document Type",
            description = "Delete a document type. Cannot delete if documents are using this type. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document type deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document type not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete - document type is in use",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentType(
            @Parameter(description = "Document type ID") @PathVariable Long id) {
        documentTypeService.deleteDocumentType(id);
        return ResponseEntity.ok(ApiResponse.success("Document type deleted successfully", null));
    }

    @Operation(
            summary = "Toggle Document Type Active Status",
            description = "Toggle the active/inactive status of a document type. Requires SUPER_ADMIN or ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status toggled successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document type not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentTypeDTO>> toggleActiveStatus(
            @Parameter(description = "Document type ID") @PathVariable Long id) {
        DocumentTypeDTO documentType = documentTypeService.toggleActiveStatus(id);
        String message = documentType.getIsActive()
                ? "Document type activated successfully"
                : "Document type deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, documentType));
    }
}
