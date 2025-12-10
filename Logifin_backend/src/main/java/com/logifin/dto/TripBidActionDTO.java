package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Size;

/**
 * DTO for accepting or rejecting a trip bid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for accepting or rejecting a bid")
public class TripBidActionDTO {

    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    @Schema(description = "Reason for rejection (required when rejecting)", example = "Terms not favorable")
    private String rejectionReason;
}
