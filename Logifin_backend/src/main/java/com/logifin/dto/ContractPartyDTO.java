package com.logifin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for Contract Party information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Contract party information")
public class ContractPartyDTO {

    @Schema(description = "Contract party ID", example = "1")
    private Long id;

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID of the party", example = "5", required = true)
    private Long userId;

    @Schema(description = "User name of the party", example = "John Doe")
    private String userName;

    @Schema(description = "User email of the party", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Timestamp when the party signed the contract")
    private LocalDateTime signedAt;
}
