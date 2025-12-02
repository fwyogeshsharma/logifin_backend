package com.logifin.service;

import com.logifin.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Trip management operations.
 */
public interface TripService {

    // ==================== CRUD Operations ====================

    /**
     * Create a new trip
     * @param requestDTO Trip creation request
     * @param userId ID of the user creating the trip
     * @return Created trip response
     */
    TripResponseDTO createTrip(TripRequestDTO requestDTO, Long userId);

    /**
     * Get trip by ID
     * @param tripId Trip ID
     * @return Trip response
     */
    TripResponseDTO getTripById(Long tripId);

    /**
     * Update an existing trip
     * @param tripId Trip ID to update
     * @param requestDTO Update request
     * @param userId ID of the user updating the trip
     * @return Updated trip response
     */
    TripResponseDTO updateTrip(Long tripId, TripRequestDTO requestDTO, Long userId);

    /**
     * Delete a trip
     * @param tripId Trip ID to delete
     */
    void deleteTrip(Long tripId);

    // ==================== Search and Pagination ====================

    /**
     * Get all trips with pagination
     * @param pageable Pagination info
     * @return Paginated trip responses
     */
    PagedResponse<TripResponseDTO> getAllTrips(Pageable pageable);

    /**
     * Search trips with criteria
     * @param criteria Search criteria
     * @param pageable Pagination info
     * @return Paginated search results
     */
    PagedResponse<TripResponseDTO> searchTrips(TripSearchCriteria criteria, Pageable pageable);

    /**
     * Search trips by keyword
     * @param keyword Keyword to search
     * @param pageable Pagination info
     * @return Paginated search results
     */
    PagedResponse<TripResponseDTO> searchByKeyword(String keyword, Pageable pageable);

    // ==================== Bulk Operations ====================

    /**
     * Bulk upload trips from CSV/Excel file
     * @param file Uploaded file (CSV, XLS, or XLSX)
     * @param userId ID of the user uploading
     * @return Bulk upload response with success/failure details
     */
    BulkUploadResponseDTO bulkUploadTrips(MultipartFile file, Long userId);

    // ==================== Export Operations ====================

    /**
     * Export trips to CSV
     * @param criteria Optional search criteria
     * @return CSV file content as bytes
     */
    byte[] exportTripsToCsv(TripSearchCriteria criteria);

    /**
     * Export trips to Excel
     * @param criteria Optional search criteria
     * @return Excel file content as bytes
     */
    byte[] exportTripsToExcel(TripSearchCriteria criteria);

    /**
     * Get CSV template for bulk upload
     * @return CSV template content as bytes
     */
    byte[] getCsvTemplate();

    /**
     * Get Excel template for bulk upload
     * @return Excel template content as bytes
     */
    byte[] getExcelTemplate();

    // ==================== Statistics ====================

    /**
     * Get trip statistics
     * @return Trip statistics DTO
     */
    TripStatisticsDTO getTripStatistics();

    // ==================== Document Operations ====================

    /**
     * Upload a document for a trip
     * @param tripId Trip ID
     * @param documentDTO Document upload request
     * @param userId ID of the user uploading
     * @return Document metadata response
     */
    TripDocumentDTO.TripDocumentMetadataDTO uploadDocument(Long tripId, TripDocumentDTO documentDTO, Long userId);

    /**
     * Get all documents for a trip
     * @param tripId Trip ID
     * @return List of document metadata
     */
    List<TripDocumentDTO.TripDocumentMetadataDTO> getDocuments(Long tripId);

    /**
     * Get document by type code for a trip
     * @param tripId Trip ID
     * @param documentTypeCode Document type code (e.g., "EWAY_BILL")
     * @return Document with data
     */
    TripDocumentDTO getDocumentByTypeCode(Long tripId, String documentTypeCode);

    /**
     * Download document
     * @param documentId Document ID
     * @return Document data
     */
    TripDocumentDTO downloadDocument(Long documentId);

    /**
     * Delete document
     * @param documentId Document ID
     */
    void deleteDocument(Long documentId);

    // ==================== Validation ====================

    /**
     * Check if E-way Bill Number exists
     * @param ewayBillNumber E-way Bill Number to check
     * @return true if exists, false otherwise
     */
    boolean ewayBillNumberExists(String ewayBillNumber);
}
