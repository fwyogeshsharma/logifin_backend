package com.logifin.service;

import com.logifin.dto.DocumentTypeDTO;

import java.util.List;

/**
 * Service interface for Document Type operations.
 */
public interface DocumentTypeService {

    /**
     * Get all document types
     * @return List of all document types ordered by sort order
     */
    List<DocumentTypeDTO> getAllDocumentTypes();

    /**
     * Get only active document types
     * @return List of active document types ordered by sort order
     */
    List<DocumentTypeDTO> getActiveDocumentTypes();

    /**
     * Get document type by ID
     * @param id Document type ID
     * @return Document type DTO
     */
    DocumentTypeDTO getDocumentTypeById(Long id);

    /**
     * Get document type by code
     * @param code Document type code
     * @return Document type DTO
     */
    DocumentTypeDTO getDocumentTypeByCode(String code);

    /**
     * Create a new document type
     * @param request Create request DTO
     * @return Created document type DTO
     */
    DocumentTypeDTO createDocumentType(DocumentTypeDTO.CreateRequest request);

    /**
     * Update an existing document type
     * @param id Document type ID
     * @param request Update request DTO
     * @return Updated document type DTO
     */
    DocumentTypeDTO updateDocumentType(Long id, DocumentTypeDTO.UpdateRequest request);

    /**
     * Delete a document type
     * @param id Document type ID
     */
    void deleteDocumentType(Long id);

    /**
     * Toggle active status of a document type
     * @param id Document type ID
     * @return Updated document type DTO
     */
    DocumentTypeDTO toggleActiveStatus(Long id);

    /**
     * Check if document type code exists
     * @param code Document type code
     * @return true if exists
     */
    boolean codeExists(String code);
}
