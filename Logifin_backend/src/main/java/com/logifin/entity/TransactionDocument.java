package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_documents", indexes = {
    @Index(name = "idx_document_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDocument extends BaseEntity {

    @NotNull(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, columnDefinition = "uuid")
    private UUID transactionId;

    @NotBlank(message = "Document type is required")
    @Column(name = "document_type", nullable = false, length = 50)
    @Builder.Default
    private String documentType = "PROOF_OF_PAYMENT";

    @NotBlank(message = "File name is required")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "MIME type is required")
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @NotNull(message = "File data is required")
    @Column(name = "file_data", nullable = false, columnDefinition = "bytea")
    private byte[] fileData;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    @Column(name = "file_size", nullable = false)
    private Integer fileSize;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private Transaction transaction;
}
