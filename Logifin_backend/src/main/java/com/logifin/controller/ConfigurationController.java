package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.ConfigurationDTO;
import com.logifin.security.CurrentUser;
import com.logifin.security.UserPrincipal;
import com.logifin.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
@Tag(name = "Configuration Management", description = "APIs for managing system configurations")
@SecurityRequirement(name = "bearerAuth")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all configurations", description = "Retrieve all system configurations")
    public ResponseEntity<ApiResponse<List<ConfigurationDTO>>> getAllConfigurations() {
        List<ConfigurationDTO> configurations = configurationService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success(configurations));
    }

    @GetMapping("/{configKey}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get configuration by key", description = "Retrieve a specific configuration by its key")
    public ResponseEntity<ApiResponse<ConfigurationDTO>> getConfigurationByKey(@PathVariable String configKey) {
        ConfigurationDTO configuration = configurationService.getConfigurationByKey(configKey);
        return ResponseEntity.ok(ApiResponse.success(configuration));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create configuration", description = "Create a new system configuration")
    public ResponseEntity<ApiResponse<ConfigurationDTO>> createConfiguration(
            @Valid @RequestBody ConfigurationDTO configurationDTO,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        ConfigurationDTO created = configurationService.saveConfiguration(configurationDTO, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Configuration created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update configuration", description = "Update an existing system configuration")
    public ResponseEntity<ApiResponse<ConfigurationDTO>> updateConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody ConfigurationDTO configurationDTO,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        configurationDTO.setId(id);
        ConfigurationDTO updated = configurationService.saveConfiguration(configurationDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Configuration updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete configuration", description = "Delete a system configuration")
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(@PathVariable Long id) {
        configurationService.deleteConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success("Configuration deleted successfully", null));
    }
}
