package com.logifin.service.impl;

import com.logifin.dto.DocumentTypeDTO;
import com.logifin.entity.DocumentType;
import com.logifin.exception.BadRequestException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.DocumentTypeRepository;
import com.logifin.repository.TripDocumentRepository;
import com.logifin.service.DocumentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of DocumentTypeService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final TripDocumentRepository tripDocumentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeDTO> getAllDocumentTypes() {
        log.debug("Fetching all document types");
        return documentTypeRepository.findAllOrdered()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeDTO> getActiveDocumentTypes() {
        log.debug("Fetching active document types");
        return documentTypeRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTypeDTO getDocumentTypeById(Long id) {
        log.debug("Fetching document type by ID: {}", id);
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "id", id));
        return mapToDTO(documentType);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTypeDTO getDocumentTypeByCode(String code) {
        log.debug("Fetching document type by code: {}", code);
        DocumentType documentType = documentTypeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "code", code));
        return mapToDTO(documentType);
    }

    @Override
    public DocumentTypeDTO createDocumentType(DocumentTypeDTO.CreateRequest request) {
        log.debug("Creating new document type with code: {}", request.getCode());

        // Normalize code to uppercase
        String normalizedCode = request.getCode().toUpperCase().trim();

        // Check if code already exists
        if (documentTypeRepository.existsByCode(normalizedCode)) {
            throw new BadRequestException("Document type with code '" + normalizedCode + "' already exists");
        }

        DocumentType documentType = DocumentType.builder()
                .code(normalizedCode)
                .displayName(request.getDisplayName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .isActive(true)
                .sortOrder(request.getSortOrder())
                .build();

        DocumentType savedDocumentType = documentTypeRepository.save(documentType);
        log.info("Document type created successfully with ID: {}", savedDocumentType.getId());

        return mapToDTO(savedDocumentType);
    }

    @Override
    public DocumentTypeDTO updateDocumentType(Long id, DocumentTypeDTO.UpdateRequest request) {
        log.debug("Updating document type with ID: {}", id);

        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "id", id));

        // Update fields if provided
        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            documentType.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getDescription() != null) {
            documentType.setDescription(request.getDescription().trim());
        }

        if (request.getIsActive() != null) {
            documentType.setIsActive(request.getIsActive());
        }

        if (request.getSortOrder() != null) {
            documentType.setSortOrder(request.getSortOrder());
        }

        DocumentType updatedDocumentType = documentTypeRepository.save(documentType);
        log.info("Document type updated successfully with ID: {}", updatedDocumentType.getId());

        return mapToDTO(updatedDocumentType);
    }

    @Override
    public void deleteDocumentType(Long id) {
        log.debug("Deleting document type with ID: {}", id);

        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "id", id));

        // Check if any documents are using this type
        long documentCount = tripDocumentRepository.countByDocumentTypeId(id);
        if (documentCount > 0) {
            throw new BadRequestException(
                    "Cannot delete document type '" + documentType.getCode() +
                    "' as it is being used by " + documentCount + " document(s). " +
                    "Consider deactivating it instead.");
        }

        documentTypeRepository.delete(documentType);
        log.info("Document type deleted successfully with ID: {}", id);
    }

    @Override
    public DocumentTypeDTO toggleActiveStatus(Long id) {
        log.debug("Toggling active status for document type with ID: {}", id);

        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "id", id));

        documentType.setIsActive(!documentType.getIsActive());

        DocumentType updatedDocumentType = documentTypeRepository.save(documentType);
        log.info("Document type active status toggled to {} for ID: {}",
                updatedDocumentType.getIsActive(), updatedDocumentType.getId());

        return mapToDTO(updatedDocumentType);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean codeExists(String code) {
        return documentTypeRepository.existsByCode(code.toUpperCase().trim());
    }

    /**
     * Map DocumentType entity to DTO
     */
    private DocumentTypeDTO mapToDTO(DocumentType documentType) {
        return DocumentTypeDTO.builder()
                .id(documentType.getId())
                .code(documentType.getCode())
                .displayName(documentType.getDisplayName())
                .description(documentType.getDescription())
                .isActive(documentType.getIsActive())
                .sortOrder(documentType.getSortOrder())
                .createdAt(documentType.getCreatedAt())
                .updatedAt(documentType.getUpdatedAt())
                .build();
    }
}
