package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller for cache management operations.
 * Only accessible by SUPER_ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Cache Management", description = "APIs for cache management (SUPER_ADMIN only)")
@SecurityRequirement(name = "Bearer Authentication")
public class CacheController {

    private final CacheService cacheService;

    @Operation(
            summary = "Get All Cache Names",
            description = "Retrieve all available cache names."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cache names retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/names")
    public ResponseEntity<ApiResponse<Set<String>>> getCacheNames() {
        Set<String> cacheNames = cacheService.getCacheNames();
        return ResponseEntity.ok(ApiResponse.success(cacheNames));
    }

    @Operation(
            summary = "Get Cache Statistics",
            description = "Get statistics for a specific cache."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cache stats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/stats/{cacheName}")
    public ResponseEntity<ApiResponse<Object>> getCacheStats(
            @Parameter(description = "Cache name") @PathVariable String cacheName) {
        Object stats = cacheService.getCacheStats(cacheName);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(
            summary = "Evict Cache Entry",
            description = "Evict a specific entry from a cache."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cache entry evicted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<ApiResponse<Void>> evictCacheEntry(
            @Parameter(description = "Cache name") @PathVariable String cacheName,
            @Parameter(description = "Cache key") @PathVariable String key) {
        cacheService.evict(cacheName, key);
        return ResponseEntity.ok(ApiResponse.success("Cache entry evicted successfully", null));
    }

    @Operation(
            summary = "Clear Specific Cache",
            description = "Clear all entries from a specific cache."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cache cleared successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<ApiResponse<Void>> clearCache(
            @Parameter(description = "Cache name") @PathVariable String cacheName) {
        cacheService.evictAll(cacheName);
        return ResponseEntity.ok(ApiResponse.success("Cache cleared successfully", null));
    }

    @Operation(
            summary = "Clear All Caches",
            description = "Clear all caches. Use with caution."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All caches cleared successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<Void>> clearAllCaches() {
        cacheService.clearAllCaches();
        return ResponseEntity.ok(ApiResponse.success("All caches cleared successfully", null));
    }

    @Operation(
            summary = "Warm Up Caches",
            description = "Pre-populate caches with frequently accessed data."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cache warm-up initiated",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/warm-up")
    public ResponseEntity<ApiResponse<Void>> warmUpCaches() {
        cacheService.warmUpCaches();
        return ResponseEntity.ok(ApiResponse.success("Cache warm-up initiated", null));
    }

    @Operation(
            summary = "Evict User Caches",
            description = "Clear all user-related caches."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User caches evicted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse<Void>> evictUserCaches() {
        cacheService.evictUserCaches();
        return ResponseEntity.ok(ApiResponse.success("User caches evicted successfully", null));
    }

    @Operation(
            summary = "Evict Role Caches",
            description = "Clear all role-related caches."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role caches evicted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/roles")
    public ResponseEntity<ApiResponse<Void>> evictRoleCaches() {
        cacheService.evictRoleCaches();
        return ResponseEntity.ok(ApiResponse.success("Role caches evicted successfully", null));
    }
}
