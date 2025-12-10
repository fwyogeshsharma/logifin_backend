package com.logifin.dto;

import com.logifin.entity.Trip;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Trip data.
 * Includes document information for all attached documents.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing trip details with document information")
public class TripResponseDTO {

    @Schema(description = "Trip ID", example = "1")
    private Long id;

    // ==================== Route Information ====================

    @Schema(description = "Pickup location address", example = "Mumbai, Maharashtra")
    private String pickup;

    @Schema(description = "Destination address", example = "Delhi, NCR")
    private String destination;

    // ==================== Parties ====================

    @Schema(description = "Name of the sender", example = "ABC Traders")
    private String sender;

    @Schema(description = "Name of the receiver", example = "XYZ Industries")
    private String receiver;

    @Schema(description = "Name of the transporter", example = "Fast Logistics Pvt Ltd")
    private String transporter;

    // ==================== Financial Terms ====================

    @Schema(description = "Loan amount for the trip", example = "100000.00")
    private BigDecimal loanAmount;

    @Schema(description = "Interest rate percentage", example = "12.50")
    private BigDecimal interestRate;

    @Schema(description = "Number of days until maturity", example = "30")
    private Integer maturityDays;

    // ==================== Cargo Details ====================

    @Schema(description = "Distance in kilometers", example = "1400.50")
    private BigDecimal distanceKm;

    @Schema(description = "Type of load/cargo", example = "Electronics")
    private String loadType;

    @Schema(description = "Weight in kilograms", example = "5000.00")
    private BigDecimal weightKg;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Trip status", example = "ACTIVE")
    private Trip.TripStatus status;

    // ==================== User & Company ====================

    @Schema(description = "Created by user ID", example = "1")
    private Long createdByUserId;

    @Schema(description = "Created by user name", example = "John Doe")
    private String createdByUserName;

    @Schema(description = "Company ID", example = "1")
    private Long companyId;

    @Schema(description = "Company name", example = "Logifin Corp")
    private String companyName;

    // ==================== Timestamps ====================

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    // ==================== Calculated Fields ====================

    @Schema(description = "Total interest amount", example = "12500.00")
    private BigDecimal totalInterestAmount;

    @Schema(description = "Total amount due (loan + interest)", example = "112500.00")
    private BigDecimal totalAmountDue;

    // ==================== Documents ====================

    @Schema(description = "All documents attached to this trip")
    private List<DocumentInfoDTO> documents;
}
