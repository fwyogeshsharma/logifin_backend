package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.security.CurrentUser;
import com.logifin.security.UserPrincipal;
import com.logifin.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST Controller for Trip Management APIs.
 * Provides endpoints for CRUD operations, bulk upload, export, and statistics.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Trip Management", description = "APIs for managing trips/shipments")
@SecurityRequirement(name = "Bearer Authentication")
public class TripController {

    private final TripService tripService;

    // ==================== CRUD Operations (TRANSPORTER Only) ====================

    @Operation(
            summary = "Create Trip",
            description = "Create a new trip. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Trip created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "E-way Bill Number already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires TRANSPORTER role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/trip")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<TripResponseDTO>> createTrip(
            @Valid @RequestBody TripRequestDTO requestDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripResponseDTO createdTrip = tripService.createTrip(requestDTO, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", createdTrip));
    }

    @Operation(
            summary = "Update Trip",
            description = "Update an existing trip. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Trip updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "E-way Bill Number already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/trip/{tripId}")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<TripResponseDTO>> updateTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId,
            @Valid @RequestBody TripRequestDTO requestDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripResponseDTO updatedTrip = tripService.updateTrip(tripId, requestDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Trip updated successfully", updatedTrip));
    }

    @Operation(
            summary = "Delete Trip",
            description = "Delete a trip. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Trip deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/trip/{tripId}")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully", null));
    }

    @Operation(
            summary = "Bulk Upload Trips",
            description = "Upload multiple trips from CSV or Excel file. Requires TRANSPORTER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Bulk upload completed (may include partial success)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping(value = "/trip/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<BulkUploadResponseDTO>> bulkUploadTrips(
            @Parameter(description = "CSV or Excel file containing trips")
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal currentUser) {
        BulkUploadResponseDTO response = tripService.bulkUploadTrips(file, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    // ==================== Read Operations (Any Authenticated User) ====================

    @Operation(
            summary = "Get Trip by ID",
            description = "Retrieve a trip by its ID. Available to any authenticated user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Trip found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Trip not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<ApiResponse<TripResponseDTO>> getTripById(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        TripResponseDTO trip = tripService.getTripById(tripId);
        return ResponseEntity.ok(ApiResponse.success(trip));
    }

    @Operation(
            summary = "Get All Trips",
            description = "Retrieve all trips with pagination and filtering. Available to any authenticated user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Trips retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/trips")
    public ResponseEntity<ApiResponse<PagedResponse<TripResponseDTO>>> getAllTrips(
            @Parameter(description = "E-way Bill Number filter") @RequestParam(required = false) String ewayBillNumber,
            @Parameter(description = "Pickup location filter") @RequestParam(required = false) String pickup,
            @Parameter(description = "Destination filter") @RequestParam(required = false) String destination,
            @Parameter(description = "Sender name filter") @RequestParam(required = false) String sender,
            @Parameter(description = "Receiver name filter") @RequestParam(required = false) String receiver,
            @Parameter(description = "Transporter filter") @RequestParam(required = false) String transporter,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Load type filter") @RequestParam(required = false) String loadType,
            @Parameter(description = "Keyword search") @RequestParam(required = false) String keyword,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String createdFrom,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String createdTo,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        // Build search criteria
        TripSearchCriteria criteria = TripSearchCriteria.builder()
                .ewayBillNumber(ewayBillNumber)
                .pickup(pickup)
                .destination(destination)
                .sender(sender)
                .receiver(receiver)
                .transporter(transporter)
                .status(status != null ? com.logifin.entity.Trip.TripStatus.valueOf(status) : null)
                .loadType(loadType)
                .keyword(keyword)
                .createdFrom(createdFrom != null ? LocalDate.parse(createdFrom) : null)
                .createdTo(createdTo != null ? LocalDate.parse(createdTo) : null)
                .build();

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<TripResponseDTO> trips = tripService.searchTrips(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @Operation(
            summary = "Get Trip Statistics",
            description = "Get trip statistics and analytics. Available to any authenticated user."
    )
    @GetMapping("/trips/statistics")
    public ResponseEntity<ApiResponse<TripStatisticsDTO>> getTripStatistics() {
        TripStatisticsDTO statistics = tripService.getTripStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    // ==================== Template Downloads ====================

    @Operation(
            summary = "Download CSV Template",
            description = "Download CSV template for bulk trip upload. Available to any authenticated user."
    )
    @GetMapping("/trip/template/csv")
    public ResponseEntity<byte[]> downloadCsvTemplate() {
        byte[] content = tripService.getCsvTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trip_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    @Operation(
            summary = "Download Excel Template",
            description = "Download Excel template for bulk trip upload. Available to any authenticated user."
    )
    @GetMapping("/trip/template/excel")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        byte[] content = tripService.getExcelTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trip_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    // ==================== Export Operations ====================

    @Operation(
            summary = "Export Trips to CSV",
            description = "Export trips to CSV file. Available to any authenticated user."
    )
    @GetMapping("/trips/export/csv")
    public ResponseEntity<byte[]> exportTripsToCsv(
            @Parameter(description = "E-way Bill Number filter") @RequestParam(required = false) String ewayBillNumber,
            @Parameter(description = "Transporter filter") @RequestParam(required = false) String transporter,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String createdFrom,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String createdTo) {

        TripSearchCriteria criteria = buildExportCriteria(ewayBillNumber, transporter, status, createdFrom, createdTo);
        byte[] content = tripService.exportTripsToCsv(criteria);

        String filename = "trips_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    @Operation(
            summary = "Export Trips to Excel",
            description = "Export trips to Excel file. Available to any authenticated user."
    )
    @GetMapping("/trips/export/excel")
    public ResponseEntity<byte[]> exportTripsToExcel(
            @Parameter(description = "E-way Bill Number filter") @RequestParam(required = false) String ewayBillNumber,
            @Parameter(description = "Transporter filter") @RequestParam(required = false) String transporter,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String createdFrom,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String createdTo) {

        TripSearchCriteria criteria = buildExportCriteria(ewayBillNumber, transporter, status, createdFrom, createdTo);
        byte[] content = tripService.exportTripsToExcel(criteria);

        String filename = "trips_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    // ==================== Document Operations ====================

    @Operation(
            summary = "Upload Trip Document",
            description = "Upload a document for a trip. Requires TRANSPORTER role."
    )
    @PostMapping("/trip/{tripId}/document")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<TripDocumentDTO.TripDocumentMetadataDTO>> uploadDocument(
            @Parameter(description = "Trip ID") @PathVariable Long tripId,
            @Valid @RequestBody TripDocumentDTO documentDTO,
            @CurrentUser UserPrincipal currentUser) {
        TripDocumentDTO.TripDocumentMetadataDTO uploaded = tripService.uploadDocument(tripId, documentDTO, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", uploaded));
    }

    @Operation(
            summary = "Get Trip Documents",
            description = "Get all documents for a trip. Available to any authenticated user."
    )
    @GetMapping("/trip/{tripId}/documents")
    public ResponseEntity<ApiResponse<List<TripDocumentDTO.TripDocumentMetadataDTO>>> getTripDocuments(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        List<TripDocumentDTO.TripDocumentMetadataDTO> documents = tripService.getDocuments(tripId);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    @Operation(
            summary = "Get Document by Type",
            description = "Get a specific document type for a trip. Available to any authenticated user."
    )
    @GetMapping("/trip/{tripId}/document/{documentTypeCode}")
    public ResponseEntity<ApiResponse<TripDocumentDTO>> getDocumentByType(
            @Parameter(description = "Trip ID") @PathVariable Long tripId,
            @Parameter(description = "Document type code (e.g., EWAY_BILL, BILTY, POD)") @PathVariable String documentTypeCode) {
        TripDocumentDTO document = tripService.getDocumentByTypeCode(tripId, documentTypeCode.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(document));
    }

    @Operation(
            summary = "Download Document",
            description = "Download a specific document. Available to any authenticated user."
    )
    @GetMapping("/trip/document/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @Parameter(description = "Document ID") @PathVariable Long documentId) {
        TripDocumentDTO document = tripService.downloadDocument(documentId);

        if (document.getDocumentBase64() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = java.util.Base64.getDecoder().decode(document.getDocumentBase64());
        String filename = document.getDocumentName() != null ? document.getDocumentName() : "document";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(document.getContentType() != null ? document.getContentType() : "application/octet-stream"))
                .body(data);
    }

    @Operation(
            summary = "Delete Document",
            description = "Delete a trip document. Requires TRANSPORTER role."
    )
    @DeleteMapping("/trip/document/{documentId}")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable Long documentId) {
        tripService.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    // ==================== Validation Endpoints ====================

    @Operation(
            summary = "Check E-way Bill Number Exists",
            description = "Check if an E-way Bill Number already exists. Available to any authenticated user."
    )
    @GetMapping("/trip/check-eway-bill/{ewayBillNumber}")
    public ResponseEntity<ApiResponse<Boolean>> checkEwayBillExists(
            @Parameter(description = "E-way Bill Number") @PathVariable String ewayBillNumber) {
        boolean exists = tripService.ewayBillNumberExists(ewayBillNumber);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // ==================== Helper Methods ====================

    private TripSearchCriteria buildExportCriteria(String ewayBillNumber, String transporter,
                                                    String status, String createdFrom, String createdTo) {
        return TripSearchCriteria.builder()
                .ewayBillNumber(ewayBillNumber)
                .transporter(transporter)
                .status(status != null ? com.logifin.entity.Trip.TripStatus.valueOf(status) : null)
                .createdFrom(createdFrom != null ? LocalDate.parse(createdFrom) : null)
                .createdTo(createdTo != null ? LocalDate.parse(createdTo) : null)
                .build();
    }
}
