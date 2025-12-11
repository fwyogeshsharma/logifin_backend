package com.logifin.dto;

import com.logifin.entity.Trip;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating or updating a Trip.
 * Documents are passed as an array with documentTypeId, documentNumber, and optional base64 data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a trip with optional documents")
public class TripRequestDTO {

    // ==================== Route Information ====================

    @NotBlank(message = "Pickup location is required")
    @Size(max = 255, message = "Pickup location must not exceed 255 characters")
    @Schema(description = "Pickup location address", example = "Mumbai, Maharashtra", required = true)
    private String pickup;

    @NotBlank(message = "Destination is required")
    @Size(max = 255, message = "Destination must not exceed 255 characters")
    @Schema(description = "Destination address", example = "Delhi, NCR", required = true)
    private String destination;

    // ==================== Parties ====================

    @NotBlank(message = "Sender name is required")
    @Size(max = 150, message = "Sender name must not exceed 150 characters")
    @Schema(description = "Name of the sender", example = "ABC Traders", required = true)
    private String sender;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 150, message = "Receiver name must not exceed 150 characters")
    @Schema(description = "Name of the receiver", example = "XYZ Industries", required = true)
    private String receiver;

    @NotBlank(message = "Transporter name is required")
    @Size(max = 150, message = "Transporter name must not exceed 150 characters")
    @Schema(description = "Name of the transporter", example = "Fast Logistics Pvt Ltd", required = true)
    private String transporter;

    // ==================== Financial Terms ====================

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "0.01", message = "Loan amount must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Loan amount must have at most 15 integer digits and 2 decimal places")
    @Schema(description = "Loan amount for the trip", example = "100000.00", required = true)
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Interest rate percentage", example = "12.50", required = true)
    private BigDecimal interestRate;

    @NotNull(message = "Maturity days is required")
    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Schema(description = "Number of days until maturity", example = "30", required = true)
    private Integer maturityDays;

    // ==================== Cargo Details (Optional) ====================

    @DecimalMin(value = "0.0", inclusive = true, message = "Distance must be 0 or greater")
    @Digits(integer = 8, fraction = 2, message = "Distance must have at most 8 integer digits and 2 decimal places")
    @Schema(description = "Distance in kilometers", example = "1400.50")
    private BigDecimal distanceKm;

    @Size(max = 100, message = "Load type must not exceed 100 characters")
    @Schema(description = "Type of load/cargo", example = "Electronics")
    private String loadType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be 0 or greater")
    @Digits(integer = 8, fraction = 2, message = "Weight must have at most 8 integer digits and 2 decimal places")
    @Schema(description = "Weight in kilograms", example = "5000.00")
    private BigDecimal weightKg;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Handle with care - fragile items")
    private String notes;

    @Schema(description = "Trip status", example = "ACTIVE")
    private Trip.TripStatus status;

    // ==================== Documents (All Optional) ====================

    @Valid
    @Schema(description = "List of documents to attach (EWAY_BILL, BILTY, TRUCK_INVOICE, POD, FINAL_INVOICE)")
    private List<DocumentUploadDTO> documents;
}
