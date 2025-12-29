package com.logifin.service;

import com.logifin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface WalletService {

    WalletDTO createWallet(CreateWalletRequest request, Long createdByUserId);

    WalletDTO getWalletByUserId(Long userId);

    WalletBalanceDTO getWalletBalance(Long userId);

    TransactionResponseDTO processManualCredit(ManualCreditRequest request, Long enteredByUserId);

    TransactionResponseDTO processManualDebit(ManualDebitRequest request, Long enteredByUserId);

    TransactionResponseDTO processTransfer(TransferRequest request, Long enteredByUserId);

    /**
     * Process financing transfer from contract wallet to transporter wallet
     * Automatically deducts portal service charge
     */
    TransactionResponseDTO processFinancingTransfer(FinancingTransferRequest request, Long enteredByUserId);

    /**
     * Process repayment transfer from shipper/contract wallet to lender wallet
     * Calculates and tracks interest payments
     */
    TransactionResponseDTO processRepaymentTransfer(RepaymentTransferRequest request, Long enteredByUserId);

    WalletStatementDTO getWalletStatement(Long userId, LocalDateTime fromDate, LocalDateTime toDate);

    Page<TransactionEntryDTO> getWalletHistory(Long userId, Pageable pageable);

    WalletDTO suspendWallet(Long userId, Long actionByUserId);

    WalletDTO activateWallet(Long userId, Long actionByUserId);

    List<WalletDTO> getAllWallets();
}
