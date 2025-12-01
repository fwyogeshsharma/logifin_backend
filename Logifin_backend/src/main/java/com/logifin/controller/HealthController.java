package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health & Info", description = "Health check and application info APIs")
public class HealthController {

    @Operation(
            summary = "Health Check",
            description = "Check if the application is running and healthy."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application is healthy",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Logifin Backend");
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @Operation(
            summary = "Application Info",
            description = "Get application information including version and Java version."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application info retrieved",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, String>>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Logifin Backend");
        info.put("version", "1.0.0");
        info.put("java", System.getProperty("java.version"));
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
