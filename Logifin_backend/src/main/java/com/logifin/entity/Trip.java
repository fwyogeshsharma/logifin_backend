package com.logifin.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Trip in the logistics system.
 * Contains route details, financial terms, and cargo specifications.
 * Documents (EWAY_BILL, BILTY, TRUCK_INVOICE, POD, FINAL_INVOICE) are stored in trip_documents table.
 * E-way Bill Number is stored in trip_documents with document_type = EWAY_BILL.
 */
@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_transporter", columnList = "transporter"),
    @Index(name = "idx_trip_created_at", columnList = "created_at"),
    @Index(name = "idx_trip_pickup", columnList = "pickup"),
    @Index(name = "idx_trip_destination", columnList = "destination"),
    @Index(name = "idx_trip_created_by", columnList = "created_by_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @NotBlank(message = "Pickup location is required")
    @Size(max = 255, message = "Pickup location must not exceed 255 characters")
    @Column(name = "pickup", nullable = false, length = 255)
    private String pickup;

    @NotBlank(message = "Destination is required")
    @Size(max = 255, message = "Destination must not exceed 255 characters")
    @Column(name = "destination", nullable = false, length = 255)
    private String destination;

    @NotBlank(message = "Sender name is required")
    @Size(max = 150, message = "Sender name must not exceed 150 characters")
    @Column(name = "sender", nullable = false, length = 150)
    private String sender;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 150, message = "Receiver name must not exceed 150 characters")
    @Column(name = "receiver", nullable = false, length = 150)
    private String receiver;

    @NotBlank(message = "Transporter name is required")
    @Size(max = 150, message = "Transporter name must not exceed 150 characters")
    @Column(name = "transporter", nullable = false, length = 150)
    private String transporter;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Loan amount must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Loan amount must have at most 15 integer digits and 2 decimal places")
    @Column(name = "loan_amount", nullable = false, precision = 17, scale = 2)
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @NotNull(message = "Maturity days is required")
    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Column(name = "maturity_days", nullable = false)
    private Integer maturityDays;

    @DecimalMin(value = "0.0", inclusive = true, message = "Distance must be 0 or greater")
    @Digits(integer = 8, fraction = 2, message = "Distance must have at most 8 integer digits and 2 decimal places")
    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Size(max = 100, message = "Load type must not exceed 100 characters")
    @Column(name = "load_type", length = 100)
    private String loadType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be 0 or greater")
    @Digits(integer = 8, fraction = 2, message = "Weight must have at most 8 integer digits and 2 decimal places")
    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    @Builder.Default
    private TripStatus status = TripStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TripBid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TripDocument> documents = new ArrayList<>();

    /**
     * Trip status enumeration
     */
    public enum TripStatus {
        ACTIVE,
        IN_TRANSIT,
        COMPLETED,
        CANCELLED
    }
}
