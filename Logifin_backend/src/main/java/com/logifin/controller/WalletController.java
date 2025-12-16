package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.security.CurrentUser;
import com.logifin.security.UserPrincipal;
import com.logifin.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "APIs for wallet and transaction management")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create wallet", description = "Create a new wallet for a user")
    public ResponseEntity<ApiResponse<WalletDTO>> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        WalletDTO wallet = walletService.createWallet(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", wallet));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    @Operation(summary = "Get wallet by user ID", description = "Retrieve wallet information for a user")
    public ResponseEntity<ApiResponse<WalletDTO>> getWalletByUserId(@PathVariable Long userId) {
        WalletDTO wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/balance/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    @Operation(summary = "Get wallet balance", description = "Get current balance of a user's wallet")
    public ResponseEntity<ApiResponse<WalletBalanceDTO>> getWalletBalance(@PathVariable Long userId) {
        WalletBalanceDTO balance = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @PostMapping("/credit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Manual credit",
               description = "Manually credit amount to user's wallet. " +
                           "The 'actualTransferDate' field is optional and represents the date when " +
                           "the amount was actually transferred to the account.")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> manualCredit(
            @Valid @RequestBody ManualCreditRequest request,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        TransactionResponseDTO response = walletService.processManualCredit(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit processed successfully", response));
    }

    @PostMapping("/debit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Manual debit",
               description = "Manually debit amount from user's wallet. " +
                           "The 'actualTransferDate' field is optional and represents the date when " +
                           "the amount was actually transferred from the account.")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> manualDebit(
            @Valid @RequestBody ManualDebitRequest request,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        TransactionResponseDTO response = walletService.processManualDebit(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Debit processed successfully", response));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Transfer between users",
               description = "Transfer amount from one user to another. " +
                           "The 'actualTransferDate' field is optional and represents the date when " +
                           "the amount was actually transferred between accounts.")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> transfer(
            @Valid @RequestBody TransferRequest request,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        TransactionResponseDTO response = walletService.processTransfer(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    @GetMapping("/statement/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    @Operation(summary = "Get wallet statement", description = "Get wallet statement for a date range")
    public ResponseEntity<ApiResponse<WalletStatementDTO>> getStatement(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        WalletStatementDTO statement = walletService.getWalletStatement(userId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    @Operation(summary = "Get wallet history", description = "Get paginated transaction history")
    public ResponseEntity<ApiResponse<Page<TransactionEntryDTO>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransactionEntryDTO> history = walletService.getWalletHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PutMapping("/suspend/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Suspend wallet", description = "Suspend a user's wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> suspendWallet(
            @PathVariable Long userId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        WalletDTO wallet = walletService.suspendWallet(userId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Wallet suspended successfully", wallet));
    }

    @PutMapping("/activate/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Activate wallet", description = "Activate a suspended wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> activateWallet(
            @PathVariable Long userId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        WalletDTO wallet = walletService.activateWallet(userId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Wallet activated successfully", wallet));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all wallets", description = "Retrieve all wallets (Admin only)")
    public ResponseEntity<ApiResponse<List<WalletDTO>>> getAllWallets() {
        List<WalletDTO> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(ApiResponse.success(wallets));
    }
}
