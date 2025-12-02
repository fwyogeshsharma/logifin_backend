package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entity representing documents attached to a Trip.
 * Supports multiple document types: E-Way Bill, Bilty, Advance Invoice, POD, Final Invoice.
 */
@Entity
@Table(name = "trip_documents", indexes = {
    @Index(name = "idx_trip_document_trip_id", columnList = "trip_id"),
    @Index(name = "idx_trip_document_document_type_id", columnList = "document_type_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @NotNull(message = "Document type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    // Keep the old column for backward compatibility during migration (will be removed later)
    @Column(name = "document_type", length = 30)
    private String documentTypeCode;

    @Size(max = 255, message = "Document name must not exceed 255 characters")
    @Column(name = "document_name", length = 255)
    private String documentName;

    @Lob
    @Column(name = "document_data", columnDefinition = "BYTEA")
    private byte[] documentData;

    @Size(max = 100, message = "Content type must not exceed 100 characters")
    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
