package com.logifin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Request DTO for lender to finance a trip
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripFinanceRequestDTO {

    @NotNull(message = "Contract ID is required")
    private Long contractId;
}
