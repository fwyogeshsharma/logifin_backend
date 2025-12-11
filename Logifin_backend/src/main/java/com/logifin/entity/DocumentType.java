package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entity representing document types for trip documents.
 * Master table containing document type definitions.
 */
@Entity
@Table(name = "document_types", indexes = {
    @Index(name = "idx_document_type_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Document type code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

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

    /**
     * Predefined document type codes (for reference and convenience)
     */
    public static final String CODE_EWAY_BILL = "EWAY_BILL";
    public static final String CODE_BILTY = "BILTY";
    public static final String CODE_TRUCK_INVOICE = "TRUCK_INVOICE";
    public static final String CODE_POD = "POD";
    public static final String CODE_FINAL_INVOICE = "FINAL_INVOICE";
}
