package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.SetRoleRequest;
import com.logifin.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Role Assignment", description = "APIs for assigning roles to users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @Operation(
            summary = "Set User Role",
            description = "Assign a role to a user. Requires ADMIN or SUPER_ADMIN role. " +
                    "Users are registered without a role and must have their role assigned using this API."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User role updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User or Role not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - requires ADMIN or SUPER_ADMIN role",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/set-role")
    public ResponseEntity<ApiResponse<Void>> setUserRole(@Valid @RequestBody SetRoleRequest request) {
        userRoleService.setUserRole(request);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", null));
    }
}
