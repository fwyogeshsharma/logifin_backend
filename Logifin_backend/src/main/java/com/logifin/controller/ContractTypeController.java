package com.logifin.controller;

import com.logifin.dto.ApiResponse;
import com.logifin.dto.ContractTypeDTO;
import com.logifin.dto.PagedResponse;
import com.logifin.service.ContractTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contract-types")
@RequiredArgsConstructor
@Tag(name = "Contract Type Management", description = "APIs for managing contract types (master data)")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractTypeController {

    private final ContractTypeService contractTypeService;

    @Operation(
            summary = "Create Contract Type",
            description = "Create a new contract type. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Contract type created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Contract type already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ContractTypeDTO>> createContractType(@Valid @RequestBody ContractTypeDTO contractTypeDTO) {
        ContractTypeDTO createdContractType = contractTypeService.createContractType(contractTypeDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contract type created successfully", createdContractType));
    }

    @Operation(
            summary = "Get Contract Type by ID",
            description = "Retrieve a contract type by its ID. Requires authentication."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractTypeDTO>> getContractTypeById(
            @Parameter(description = "Contract Type ID") @PathVariable Long id) {
        ContractTypeDTO contractType = contractTypeService.getContractTypeById(id);
        return ResponseEntity.ok(ApiResponse.success("Contract type retrieved successfully", contractType));
    }

    @Operation(
            summary = "Get Contract Type by Name",
            description = "Retrieve a contract type by its type name. Requires authentication."
    )
    @GetMapping("/by-name/{typeName}")
    public ResponseEntity<ApiResponse<ContractTypeDTO>> getContractTypeByTypeName(
            @Parameter(description = "Type Name") @PathVariable String typeName) {
        ContractTypeDTO contractType = contractTypeService.getContractTypeByTypeName(typeName);
        return ResponseEntity.ok(ApiResponse.success("Contract type retrieved successfully", contractType));
    }

    @Operation(
            summary = "Get All Contract Types",
            description = "Retrieve all contract types. Requires authentication."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractTypeDTO>>> getAllContractTypes() {
        List<ContractTypeDTO> contractTypes = contractTypeService.getAllContractTypes();
        return ResponseEntity.ok(ApiResponse.success("Contract types retrieved successfully", contractTypes));
    }

    @Operation(
            summary = "Get All Contract Types Ordered by Party Count",
            description = "Retrieve all contract types ordered by party count. Requires authentication."
    )
    @GetMapping("/ordered")
    public ResponseEntity<ApiResponse<List<ContractTypeDTO>>> getAllContractTypesOrderedByPartyCount() {
        List<ContractTypeDTO> contractTypes = contractTypeService.getAllContractTypesOrderedByPartyCount();
        return ResponseEntity.ok(ApiResponse.success("Contract types retrieved successfully", contractTypes));
    }

    @Operation(
            summary = "Get Contract Types by Party Count",
            description = "Retrieve contract types by party count. Requires authentication."
    )
    @GetMapping("/by-party-count/{partyCount}")
    public ResponseEntity<ApiResponse<List<ContractTypeDTO>>> getContractTypesByPartyCount(
            @Parameter(description = "Party Count") @PathVariable Integer partyCount) {
        List<ContractTypeDTO> contractTypes = contractTypeService.getContractTypesByPartyCount(partyCount);
        return ResponseEntity.ok(ApiResponse.success("Contract types retrieved successfully", contractTypes));
    }

    @Operation(
            summary = "Get All Contract Types (Paginated)",
            description = "Retrieve all contract types with pagination. Requires authentication."
    )
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<ContractTypeDTO>>> getAllContractTypesPaginated(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "partyCount") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ContractTypeDTO> contractTypes = contractTypeService.getAllContractTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success("Contract types retrieved successfully", contractTypes));
    }

    @Operation(
            summary = "Update Contract Type",
            description = "Update an existing contract type. Requires SUPER_ADMIN role."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ContractTypeDTO>> updateContractType(
            @Parameter(description = "Contract Type ID") @PathVariable Long id,
            @Valid @RequestBody ContractTypeDTO contractTypeDTO) {
        ContractTypeDTO updatedContractType = contractTypeService.updateContractType(id, contractTypeDTO);
        return ResponseEntity.ok(ApiResponse.success("Contract type updated successfully", updatedContractType));
    }

    @Operation(
            summary = "Delete Contract Type",
            description = "Delete a contract type. Requires SUPER_ADMIN role."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteContractType(
            @Parameter(description = "Contract Type ID") @PathVariable Long id) {
        contractTypeService.deleteContractType(id);
        return ResponseEntity.ok(ApiResponse.success("Contract type deleted successfully", null));
    }
}
