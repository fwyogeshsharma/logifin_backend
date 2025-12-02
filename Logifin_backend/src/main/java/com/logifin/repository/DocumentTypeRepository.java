package com.logifin.repository;

import com.logifin.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DocumentType entity operations.
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

    /**
     * Find document type by code
     * @param code Document type code
     * @return Optional DocumentType
     */
    Optional<DocumentType> findByCode(String code);

    /**
     * Find document type by code ignoring case
     * @param code Document type code
     * @return Optional DocumentType
     */
    Optional<DocumentType> findByCodeIgnoreCase(String code);

    /**
     * Check if document type exists by code
     * @param code Document type code
     * @return true if exists
     */
    boolean existsByCode(String code);

    /**
     * Find all active document types
     * @return List of active document types
     */
    List<DocumentType> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Find all document types ordered by sort order
     * @return List of all document types
     */
    @Query("SELECT dt FROM DocumentType dt ORDER BY dt.sortOrder ASC NULLS LAST, dt.displayName ASC")
    List<DocumentType> findAllOrdered();
}
