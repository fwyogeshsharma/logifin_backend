package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for counter offer on a trip bid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for making a counter offer on a bid")
public class TripBidCounterOfferDTO {

    @NotNull(message = "Counter amount is required")
    @DecimalMin(value = "0.01", message = "Counter amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Counter amount must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Counter offer amount", example = "110000.00", required = true)
    private BigDecimal counterAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Counter interest rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Counter interest rate must not exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Counter interest rate must have at most 3 integer digits and 2 decimal places")
    @Schema(description = "Counter offer interest rate percentage", example = "10.00")
    private BigDecimal counterInterestRate;

    @Min(value = 1, message = "Counter maturity days must be at least 1")
    @Max(value = 365, message = "Counter maturity days must not exceed 365")
    @Schema(description = "Counter offer maturity days", example = "45")
    private Integer counterMaturityDays;

    @Size(max = 1000, message = "Counter notes must not exceed 1000 characters")
    @Schema(description = "Notes for counter offer", example = "Adjusted terms based on market rates")
    private String counterNotes;
}
