package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating/updating Trip Bids.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a trip bid")
public class TripBidRequestDTO {

    @NotNull(message = "Trip ID is required")
    @Schema(description = "ID of the trip to bid on", example = "1", required = true)
    private Long tripId;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Bid amount must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Bid amount offered", example = "100000.00", required = true)
    private BigDecimal bidAmount;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    @Schema(description = "Currency code", example = "INR", defaultValue = "INR")
    private String currency = "INR";

    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Proposed interest rate percentage", example = "12.50")
    private BigDecimal interestRate;

    @Min(value = 1, message = "Maturity days must be at least 1")
    @Max(value = 365, message = "Maturity days must not exceed 365")
    @Schema(description = "Proposed number of maturity days", example = "30")
    private Integer maturityDays;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes or remarks", example = "Flexible on terms if needed")
    private String notes;

    @Schema(description = "Bid expiry date-time (optional, defaults to 7 days)", example = "2024-12-31T23:59:59")
    private LocalDateTime expiresAt;
}
