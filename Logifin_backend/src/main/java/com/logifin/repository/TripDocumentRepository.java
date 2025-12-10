package com.logifin.repository;

import com.logifin.entity.DocumentType;
import com.logifin.entity.TripDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TripDocument entity.
 */
@Repository
public interface TripDocumentRepository extends JpaRepository<TripDocument, Long> {

    /**
     * Find all documents for a trip
     */
    List<TripDocument> findByTripId(Long tripId);

    /**
     * Find all documents for a trip by document type entity
     */
    List<TripDocument> findByTripIdAndDocumentType(Long tripId, DocumentType documentType);

    /**
     * Find all documents for a trip by document type code
     */
    @Query("SELECT td FROM TripDocument td WHERE td.trip.id = :tripId AND td.documentType.code = :documentTypeCode")
    List<TripDocument> findByTripIdAndDocumentTypeCode(@Param("tripId") Long tripId,
                                                        @Param("documentTypeCode") String documentTypeCode);

    /**
     * Find a specific document by trip ID and document type code (latest one)
     */
    @Query("SELECT td FROM TripDocument td WHERE td.trip.id = :tripId AND td.documentType.code = :documentTypeCode ORDER BY td.createdAt DESC")
    List<TripDocument> findByTripIdAndDocumentTypeCodeOrderByCreatedAtDesc(
            @Param("tripId") Long tripId, @Param("documentTypeCode") String documentTypeCode);

    /**
     * Check if a document type exists for a trip
     */
    @Query("SELECT CASE WHEN COUNT(td) > 0 THEN true ELSE false END FROM TripDocument td WHERE td.trip.id = :tripId AND td.documentType.code = :documentTypeCode")
    boolean existsByTripIdAndDocumentTypeCode(@Param("tripId") Long tripId,
                                               @Param("documentTypeCode") String documentTypeCode);

    /**
     * Count documents by trip ID
     */
    long countByTripId(Long tripId);

    /**
     * Count documents by trip ID and document type code
     */
    @Query("SELECT COUNT(td) FROM TripDocument td WHERE td.trip.id = :tripId AND td.documentType.code = :documentTypeCode")
    long countByTripIdAndDocumentTypeCode(@Param("tripId") Long tripId,
                                           @Param("documentTypeCode") String documentTypeCode);

    /**
     * Delete all documents for a trip
     */
    @Modifying
    @Query("DELETE FROM TripDocument td WHERE td.trip.id = :tripId")
    void deleteByTripId(@Param("tripId") Long tripId);

    /**
     * Delete documents by trip ID and document type code
     */
    @Modifying
    @Query("DELETE FROM TripDocument td WHERE td.trip.id = :tripId AND td.documentType.code = :documentTypeCode")
    void deleteByTripIdAndDocumentTypeCode(@Param("tripId") Long tripId,
                                            @Param("documentTypeCode") String documentTypeCode);

    /**
     * Delete documents by trip ID and document type ID
     */
    @Modifying
    @Query("DELETE FROM TripDocument td WHERE td.trip.id = :tripId AND td.documentType.id = :documentTypeId")
    void deleteByTripIdAndDocumentTypeId(@Param("tripId") Long tripId,
                                          @Param("documentTypeId") Long documentTypeId);

    /**
     * Count documents by document type ID
     */
    @Query("SELECT COUNT(td) FROM TripDocument td WHERE td.documentType.id = :documentTypeId")
    long countByDocumentTypeId(@Param("documentTypeId") Long documentTypeId);
}
