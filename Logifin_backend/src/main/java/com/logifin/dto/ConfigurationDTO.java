package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationDTO {

    private Long id;

    @NotBlank(message = "Config key is required")
    @Size(max = 100, message = "Config key must not exceed 100 characters")
    private String configKey;

    @NotBlank(message = "Config value is required")
    private String configValue;

    @NotBlank(message = "Config type is required")
    private String configType;

    private String description;

    private Boolean isActive;

    private Long createdByUserId;

    private Long updatedByUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
