package com.logifin.repository;

import com.logifin.entity.TransactionDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionDocumentRepository extends JpaRepository<TransactionDocument, Long> {

    List<TransactionDocument> findByTransactionId(UUID transactionId);

    @Query("SELECT td.id, td.transactionId, td.documentType, td.fileName, td.mimeType, " +
           "td.fileSize, td.uploadedAt FROM TransactionDocument td WHERE td.transactionId = :transactionId")
    List<Object[]> findDocumentInfoByTransactionId(@Param("transactionId") UUID transactionId);

    List<TransactionDocument> findByDocumentType(String documentType);

    boolean existsByTransactionId(UUID transactionId);

    long countByTransactionId(UUID transactionId);
}
