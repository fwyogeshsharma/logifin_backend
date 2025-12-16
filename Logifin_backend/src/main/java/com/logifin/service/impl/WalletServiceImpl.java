package com.logifin.service.impl;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.*;
import com.logifin.repository.*;
import com.logifin.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEntryRepository entryRepository;
    private final ManualTransferRequestRepository manualRequestRepository;
    private final TransactionDocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WalletDTO createWallet(CreateWalletRequest request, Long createdByUserId) {
        log.info("Creating wallet for user: {}", request.getUserId());

        if (walletRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("Wallet already exists for user: " + request.getUserId());
        }

        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User not found with ID: " + request.getUserId());
        }

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "INR")
                .status("ACTIVE")
                .build();

        wallet = walletRepository.save(wallet);
        log.info("Wallet created successfully with ID: {}", wallet.getId());

        return mapToWalletDTO(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletDTO getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        return mapToWalletDTO(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceDTO getWalletBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        BigDecimal balance = entryRepository.getLatestBalanceSnapshot(wallet.getId())
                .orElse(entryRepository.calculateWalletBalance(wallet.getId()));

        return WalletBalanceDTO.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .currencyCode(wallet.getCurrencyCode())
                .availableBalance(balance)
                .status(wallet.getStatus())
                .asOfTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponseDTO processManualCredit(ManualCreditRequest request, Long enteredByUserId) {
        log.info("Processing manual credit for user: {}, amount: {}", request.getUserId(), request.getAmount());

        Wallet wallet = walletRepository.findByUserIdWithLock(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + request.getUserId()));

        validateWalletStatus(wallet);

        Transaction transaction = Transaction.builder()
                .transactionType("MANUAL_CREDIT")
                .status("COMPLETED")
                .description("Manual credit of " + request.getAmount() + " " + wallet.getCurrencyCode())
                .createdByUserId(enteredByUserId)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .actualTransferDate(request.getActualTransferDate())
                .build();

        transaction = transactionRepository.save(transaction);

        BigDecimal currentBalance = getCurrentBalance(wallet.getId());
        BigDecimal newBalance = currentBalance.add(request.getAmount());

        TransactionEntry entry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(wallet.getId())
                .entryType("CREDIT")
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .entrySequence((short) 1)
                .build();

        entry = entryRepository.save(entry);

        ManualTransferRequest manualRequest = ManualTransferRequest.builder()
                .transactionId(transaction.getTransactionId())
                .requestType("CREDIT")
                .toUserId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .enteredByUserId(enteredByUserId)
                .enteredAt(LocalDateTime.now())
                .build();

        manualRequest = manualRequestRepository.save(manualRequest);

        if (request.getProofImageBase64() != null && !request.getProofImageBase64().isEmpty()) {
            saveDocument(transaction.getTransactionId(), request.getProofImageBase64(),
                    request.getProofImageFileName(), request.getProofImageMimeType());
        }

        log.info("Manual credit processed successfully. Transaction ID: {}", transaction.getTransactionId());

        return mapToTransactionResponseDTO(transaction, Collections.singletonList(entry), manualRequest);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponseDTO processManualDebit(ManualDebitRequest request, Long enteredByUserId) {
        log.info("Processing manual debit for user: {}, amount: {}", request.getUserId(), request.getAmount());

        Wallet wallet = walletRepository.findByUserIdWithLock(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + request.getUserId()));

        validateWalletStatus(wallet);

        BigDecimal currentBalance = getCurrentBalance(wallet.getId());

        // Allow negative balance - represents borrowing/credit extended to user
        // No insufficient balance check - user can go into debt

        BigDecimal newBalance = currentBalance.subtract(request.getAmount());
        String description = "Manual debit of " + request.getAmount() + " " + wallet.getCurrencyCode();

        // Add borrowing indicator if balance goes negative
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            description += " (Borrowing: " + newBalance.abs() + " " + wallet.getCurrencyCode() + ")";
        }

        Transaction transaction = Transaction.builder()
                .transactionType("MANUAL_DEBIT")
                .status("COMPLETED")
                .description(description)
                .createdByUserId(enteredByUserId)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .actualTransferDate(request.getActualTransferDate())
                .build();

        transaction = transactionRepository.save(transaction);

        // newBalance already calculated above

        TransactionEntry entry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(wallet.getId())
                .entryType("DEBIT")
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .entrySequence((short) 1)
                .build();

        entry = entryRepository.save(entry);

        ManualTransferRequest manualRequest = ManualTransferRequest.builder()
                .transactionId(transaction.getTransactionId())
                .requestType("DEBIT")
                .fromUserId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .enteredByUserId(enteredByUserId)
                .enteredAt(LocalDateTime.now())
                .build();

        manualRequest = manualRequestRepository.save(manualRequest);

        if (request.getProofImageBase64() != null && !request.getProofImageBase64().isEmpty()) {
            saveDocument(transaction.getTransactionId(), request.getProofImageBase64(),
                    request.getProofImageFileName(), request.getProofImageMimeType());
        }

        log.info("Manual debit processed successfully. Transaction ID: {}", transaction.getTransactionId());

        return mapToTransactionResponseDTO(transaction, Collections.singletonList(entry), manualRequest);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponseDTO processTransfer(TransferRequest request, Long enteredByUserId) {
        log.info("Processing transfer from user: {} to user: {}, amount: {}",
                request.getFromUserId(), request.getToUserId(), request.getAmount());

        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new InvalidTransactionException("Cannot transfer to the same wallet");
        }

        Wallet fromWallet = walletRepository.findByUserIdWithLock(request.getFromUserId())
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found for user: " + request.getFromUserId()));

        Wallet toWallet = walletRepository.findByUserIdWithLock(request.getToUserId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found for user: " + request.getToUserId()));

        validateWalletStatus(fromWallet);
        validateWalletStatus(toWallet);

        if (!fromWallet.getCurrencyCode().equals(toWallet.getCurrencyCode())) {
            throw new InvalidTransactionException("Currency mismatch between wallets");
        }

        BigDecimal fromBalance = getCurrentBalance(fromWallet.getId());

        // Allow negative balance - represents borrowing/credit extended to sender
        // No insufficient balance check - sender can go into debt

        BigDecimal fromNewBalance = fromBalance.subtract(request.getAmount());
        String description = "Transfer of " + request.getAmount() + " " + fromWallet.getCurrencyCode() +
                " from user " + request.getFromUserId() + " to user " + request.getToUserId();

        // Add borrowing indicator if sender's balance goes negative
        if (fromNewBalance.compareTo(BigDecimal.ZERO) < 0) {
            description += " (Sender borrowing: " + fromNewBalance.abs() + " " + fromWallet.getCurrencyCode() + ")";
        }

        Transaction transaction = Transaction.builder()
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .description(description)
                .createdByUserId(enteredByUserId)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .actualTransferDate(request.getActualTransferDate())
                .build();

        transaction = transactionRepository.save(transaction);

        // fromNewBalance already calculated above
        TransactionEntry debitEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(fromWallet.getId())
                .entryType("DEBIT")
                .amount(request.getAmount())
                .balanceAfter(fromNewBalance)
                .entrySequence((short) 1)
                .build();

        debitEntry = entryRepository.save(debitEntry);

        BigDecimal toBalance = getCurrentBalance(toWallet.getId());
        BigDecimal toNewBalance = toBalance.add(request.getAmount());
        TransactionEntry creditEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(toWallet.getId())
                .entryType("CREDIT")
                .amount(request.getAmount())
                .balanceAfter(toNewBalance)
                .entrySequence((short) 2)
                .build();

        creditEntry = entryRepository.save(creditEntry);

        ManualTransferRequest manualRequest = ManualTransferRequest.builder()
                .transactionId(transaction.getTransactionId())
                .requestType("TRANSFER")
                .fromUserId(request.getFromUserId())
                .toUserId(request.getToUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .enteredByUserId(enteredByUserId)
                .enteredAt(LocalDateTime.now())
                .build();

        manualRequest = manualRequestRepository.save(manualRequest);

        if (request.getProofImageBase64() != null && !request.getProofImageBase64().isEmpty()) {
            saveDocument(transaction.getTransactionId(), request.getProofImageBase64(),
                    request.getProofImageFileName(), request.getProofImageMimeType());
        }

        log.info("Transfer processed successfully. Transaction ID: {}", transaction.getTransactionId());

        return mapToTransactionResponseDTO(transaction, Arrays.asList(debitEntry, creditEntry), manualRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletStatementDTO getWalletStatement(Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        List<TransactionEntry> entries = entryRepository.findWalletEntriesByDateRange(
                wallet.getId(), fromDate, toDate);

        BigDecimal openingBalance = BigDecimal.ZERO;
        if (!entries.isEmpty()) {
            TransactionEntry firstEntry = entries.get(entries.size() - 1);
            openingBalance = firstEntry.getBalanceAfter().subtract(
                    firstEntry.getEntryType().equals("CREDIT") ?
                            firstEntry.getAmount() : firstEntry.getAmount().negate());
        }

        BigDecimal closingBalance = entries.isEmpty() ? openingBalance :
                entries.get(0).getBalanceAfter();

        List<TransactionEntryDTO> entryDTOs = entries.stream()
                .map(this::mapToTransactionEntryDTO)
                .collect(Collectors.toList());

        return WalletStatementDTO.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .currencyCode(wallet.getCurrencyCode())
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .fromDate(fromDate)
                .toDate(toDate)
                .entries(entryDTOs)
                .totalEntries(entries.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionEntryDTO> getWalletHistory(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        Page<TransactionEntry> entries = entryRepository.findByWalletIdOrderByCreatedAtDesc(
                wallet.getId(), pageable);

        return entries.map(this::mapToTransactionEntryDTO);
    }

    @Override
    @Transactional
    public WalletDTO suspendWallet(Long userId, Long actionByUserId) {
        log.info("Suspending wallet for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setStatus("SUSPENDED");
        wallet = walletRepository.save(wallet);

        log.info("Wallet suspended successfully for user: {}", userId);
        return mapToWalletDTO(wallet);
    }

    @Override
    @Transactional
    public WalletDTO activateWallet(Long userId, Long actionByUserId) {
        log.info("Activating wallet for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setStatus("ACTIVE");
        wallet = walletRepository.save(wallet);

        log.info("Wallet activated successfully for user: {}", userId);
        return mapToWalletDTO(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletDTO> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(this::mapToWalletDTO)
                .collect(Collectors.toList());
    }

    private BigDecimal getCurrentBalance(Long walletId) {
        return entryRepository.getLatestBalanceSnapshot(walletId)
                .orElse(entryRepository.calculateWalletBalance(walletId));
    }

    private void validateWalletStatus(Wallet wallet) {
        if ("SUSPENDED".equals(wallet.getStatus())) {
            throw new WalletSuspendedException("Wallet is suspended for user: " + wallet.getUserId());
        }
        if ("CLOSED".equals(wallet.getStatus())) {
            throw new WalletSuspendedException("Wallet is closed for user: " + wallet.getUserId());
        }
    }

    private void saveDocument(UUID transactionId, String base64Data, String fileName, String mimeType) {
        try {
            byte[] fileData = Base64.getDecoder().decode(base64Data);

            TransactionDocument document = TransactionDocument.builder()
                    .transactionId(transactionId)
                    .documentType("PROOF_OF_PAYMENT")
                    .fileName(fileName != null ? fileName : "document.jpg")
                    .mimeType(mimeType != null ? mimeType : "image/jpeg")
                    .fileData(fileData)
                    .fileSize(fileData.length)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            documentRepository.save(document);
            log.info("Document saved for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Error saving document for transaction: {}", transactionId, e);
        }
    }

    private WalletDTO mapToWalletDTO(Wallet wallet) {
        BigDecimal balance = getCurrentBalance(wallet.getId());

        return WalletDTO.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .currencyCode(wallet.getCurrencyCode())
                .status(wallet.getStatus())
                .currentBalance(balance)
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionEntryDTO mapToTransactionEntryDTO(TransactionEntry entry) {
        return TransactionEntryDTO.builder()
                .entryId(entry.getId())
                .transactionId(entry.getTransactionId())
                .walletId(entry.getWalletId())
                .userId(entry.getWallet() != null ? entry.getWallet().getUserId() : null)
                .entryType(entry.getEntryType())
                .amount(entry.getAmount())
                .balanceAfter(entry.getBalanceAfter())
                .entrySequence(entry.getEntrySequence())
                .createdAt(entry.getCreatedAt())
                .build();
    }

    private TransactionResponseDTO mapToTransactionResponseDTO(Transaction transaction,
                                                               List<TransactionEntry> entries,
                                                               ManualTransferRequest manualRequest) {
        List<TransactionEntryDTO> entryDTOs = entries.stream()
                .map(this::mapToTransactionEntryDTO)
                .collect(Collectors.toList());

        ManualTransferRequestDTO requestDTO = ManualTransferRequestDTO.builder()
                .requestId(manualRequest.getId())
                .transactionId(manualRequest.getTransactionId())
                .requestType(manualRequest.getRequestType())
                .fromUserId(manualRequest.getFromUserId())
                .toUserId(manualRequest.getToUserId())
                .amount(manualRequest.getAmount())
                .paymentMethod(manualRequest.getPaymentMethod())
                .referenceNumber(manualRequest.getReferenceNumber())
                .remarks(manualRequest.getRemarks())
                .enteredByUserId(manualRequest.getEnteredByUserId())
                .enteredAt(manualRequest.getEnteredAt())
                .build();

        boolean hasDocuments = documentRepository.existsByTransactionId(transaction.getTransactionId());

        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .amount(manualRequest.getAmount())
                .createdByUserId(transaction.getCreatedByUserId())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .actualTransferDate(transaction.getActualTransferDate())
                .entries(entryDTOs)
                .manualRequest(requestDTO)
                .hasDocuments(hasDocuments)
                .build();
    }
}
