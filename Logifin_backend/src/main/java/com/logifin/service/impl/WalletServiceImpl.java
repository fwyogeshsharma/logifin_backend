package com.logifin.service.impl;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.*;
import com.logifin.repository.*;
import com.logifin.service.ConfigurationService;
import com.logifin.service.WalletService;
import com.logifin.util.FinancialCalculationUtil;
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
    private final ConfigurationService configurationService;
    private final TripFinancialRepository tripFinancialRepository;
    private final TripRepository tripRepository;
    private final ContractRepository contractRepository;

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
        log.info("Processing transfer from user: {} to user: {}, amount: {}, tripId: {}",
                request.getFromUserId(), request.getToUserId(), request.getAmount(), request.getTripId());

        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new InvalidTransactionException("Cannot transfer to the same wallet");
        }

        // Handle trip-based transfer with automatic contract discovery
        Long tripId = null;
        Long contractId = null;
        String tripInfo = "";

        if (request.getTripId() != null) {
            Trip trip = tripRepository.findById(request.getTripId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", request.getTripId()));

            if (trip.getContract() == null) {
                throw new InvalidTransactionException(
                        "Trip #" + request.getTripId() + " does not have a contract assigned. " +
                        "Please ensure the transporter has accepted a lender's financing proposal for this trip.");
            }

            tripId = trip.getId();
            contractId = trip.getContract().getId();
            tripInfo = " for Trip #" + tripId + " (Contract #" + contractId + ")";

            log.info("Transfer linked to Trip #{} and Contract #{}", tripId, contractId);
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
                " from user " + request.getFromUserId() + " to user " + request.getToUserId() + tripInfo;

        // Add borrowing indicator if sender's balance goes negative
        if (fromNewBalance.compareTo(BigDecimal.ZERO) < 0) {
            description += " (Sender borrowing: " + fromNewBalance.abs() + " " + fromWallet.getCurrencyCode() + ")";
        }

        Transaction transaction = Transaction.builder()
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .description(description)
                .tripId(tripId)
                .contractId(contractId)
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

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponseDTO processFinancingTransfer(FinancingTransferRequest request, Long enteredByUserId) {
        log.info("Processing financing transfer for contract: {}, trip: {}, amount: {}",
                request.getContractId(), request.getTripId(), request.getAmount());

        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new InvalidTransactionException("Cannot transfer to the same wallet");
        }

        Wallet fromWallet = walletRepository.findByUserIdWithLock(request.getFromUserId())
                .orElseThrow(() -> new WalletNotFoundException("Contract wallet not found for user: " + request.getFromUserId()));

        Wallet toWallet = walletRepository.findByUserIdWithLock(request.getToUserId())
                .orElseThrow(() -> new WalletNotFoundException("Transporter wallet not found for user: " + request.getToUserId()));

        validateWalletStatus(fromWallet);
        validateWalletStatus(toWallet);

        if (!fromWallet.getCurrencyCode().equals(toWallet.getCurrencyCode())) {
            throw new InvalidTransactionException("Currency mismatch between wallets");
        }

        // Validate trip and contract existence
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + request.getTripId()));

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + request.getContractId()));

        // Check if trip has already been financed
        if (tripFinancialRepository.existsByTripId(request.getTripId())) {
            throw new InvalidTransactionException("Trip has already been financed: " + request.getTripId());
        }

        // Get portal service charge percentage from configuration
        BigDecimal serviceChargePercentage = configurationService.getPortalServiceChargePercentage();

        // IMPORTANT: Store the ORIGINAL amount for interest calculation
        BigDecimal originalPrincipalAmount = request.getAmount(); // e.g., 500

        // Calculate platform fee and net amount
        BigDecimal platformFee = FinancialCalculationUtil.calculatePortalServiceCharge(originalPrincipalAmount, serviceChargePercentage); // e.g., 2.5
        BigDecimal netAmountToTransporter = originalPrincipalAmount.subtract(platformFee); // e.g., 497.5

        // Get super admin wallet for platform fee
        User superAdmin = userRepository.findFirstSuperAdmin()
                .orElseThrow(() -> new ResourceNotFoundException("Super Admin user not found"));

        Wallet superAdminWallet = walletRepository.findByUserIdWithLock(superAdmin.getId())
                .orElseThrow(() -> new WalletNotFoundException("Super Admin wallet not found"));

        validateWalletStatus(superAdminWallet);

        BigDecimal fromBalance = getCurrentBalance(fromWallet.getId());
        BigDecimal fromNewBalance = fromBalance.subtract(originalPrincipalAmount); // Debit full amount from trust account

        String description = String.format("Financing transfer for trip %d (Contract: %d) - Original: %s, Platform fee: %s (%s%%) to Super Admin, Net to transporter: %s %s. Interest will be calculated on original amount of %s",
                request.getTripId(), request.getContractId(),
                originalPrincipalAmount, platformFee, serviceChargePercentage, netAmountToTransporter, fromWallet.getCurrencyCode(), originalPrincipalAmount);

        Transaction transaction = Transaction.builder()
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .description(description)
                .tripId(request.getTripId())
                .contractId(request.getContractId())
                .transactionPurpose("FINANCING")
                .grossAmount(originalPrincipalAmount)           // 500
                .platformFeeAmount(platformFee)                 // 2.5
                .netAmount(netAmountToTransporter)              // 497.5
                .createdByUserId(enteredByUserId)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .actualTransferDate(request.getActualTransferDate())
                .build();

        transaction = transactionRepository.save(transaction);

        // Entry 1: Debit from trust account (FULL ORIGINAL AMOUNT - 500)
        TransactionEntry debitEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(fromWallet.getId())
                .entryType("DEBIT")
                .amount(originalPrincipalAmount)  // 500
                .balanceAfter(fromNewBalance)
                .entrySequence((short) 1)
                .build();

        debitEntry = entryRepository.save(debitEntry);

        // Entry 2: Credit to transporter wallet (NET AMOUNT after platform fee - 497.5)
        BigDecimal toBalance = getCurrentBalance(toWallet.getId());
        BigDecimal toNewBalance = toBalance.add(netAmountToTransporter);

        TransactionEntry creditEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(toWallet.getId())
                .entryType("CREDIT")
                .amount(netAmountToTransporter)  // 497.5
                .balanceAfter(toNewBalance)
                .entrySequence((short) 2)
                .build();

        creditEntry = entryRepository.save(creditEntry);

        // Entry 3: Credit platform fee to super admin wallet (2.5)
        BigDecimal superAdminBalance = getCurrentBalance(superAdminWallet.getId());
        BigDecimal superAdminNewBalance = superAdminBalance.add(platformFee);

        TransactionEntry platformFeeEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(superAdminWallet.getId())
                .entryType("CREDIT")
                .amount(platformFee)  // 2.5
                .balanceAfter(superAdminNewBalance)
                .entrySequence((short) 3)
                .build();

        platformFeeEntry = entryRepository.save(platformFeeEntry);

        // Create TripFinancial record to track original amount for interest calculation
        TripFinancial tripFinancial = TripFinancial.builder()
                .tripId(request.getTripId())
                .contractId(request.getContractId())
                .financingTransactionId(transaction.getTransactionId())
                .originalPrincipalAmount(originalPrincipalAmount)  // 500 - for interest calculation
                .platformFeeAmount(platformFee)                     // 2.5
                .netAmountToTransporter(netAmountToTransporter)     // 497.5
                .interestRate(trip.getInterestRate())
                .financingDate(LocalDateTime.now())
                .status("FINANCED")
                .build();

        tripFinancialRepository.save(tripFinancial);

        log.info("TripFinancial record created: Original amount {} for interest calculation, Net amount {} to transporter",
                originalPrincipalAmount, netAmountToTransporter);

        ManualTransferRequest manualRequest = ManualTransferRequest.builder()
                .transactionId(transaction.getTransactionId())
                .requestType("TRANSFER")
                .fromUserId(request.getFromUserId())
                .toUserId(request.getToUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks() != null ? request.getRemarks() :
                        String.format("Financing for trip %d, Service charge: %s", request.getTripId(), platformFee))
                .enteredByUserId(enteredByUserId)
                .enteredAt(LocalDateTime.now())
                .build();

        manualRequest = manualRequestRepository.save(manualRequest);

        if (request.getProofImageBase64() != null && !request.getProofImageBase64().isEmpty()) {
            saveDocument(transaction.getTransactionId(), request.getProofImageBase64(),
                    request.getProofImageFileName(), request.getProofImageMimeType());
        }

        log.info("Financing transfer processed successfully. Transaction ID: {}, Service charge: {} credited to Super Admin",
                transaction.getTransactionId(), platformFee);

        return mapToTransactionResponseDTO(transaction, Arrays.asList(debitEntry, creditEntry, platformFeeEntry), manualRequest);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponseDTO processRepaymentTransfer(RepaymentTransferRequest request, Long enteredByUserId) {
        log.info("Processing repayment transfer for contract: {}, trip: {}, interest: {}",
                request.getContractId(), request.getTripId(), request.getInterestAmount());

        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new InvalidTransactionException("Cannot transfer to the same wallet");
        }

        // Get the TripFinancial record to retrieve original principal amount
        TripFinancial tripFinancial = tripFinancialRepository.findByTripId(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip financial record not found for trip: " + request.getTripId()));

        if (!"FINANCED".equals(tripFinancial.getStatus())) {
            throw new InvalidTransactionException("Trip has already been repaid or is in invalid state: " + tripFinancial.getStatus());
        }

        // CRITICAL: Use ORIGINAL principal amount for interest calculation, not net amount
        BigDecimal originalPrincipalAmount = tripFinancial.getOriginalPrincipalAmount(); // e.g., 500 (not 497.5)

        // Calculate interest on ORIGINAL amount
        long daysUsed = FinancialCalculationUtil.calculateDaysBetween(tripFinancial.getFinancingDate(), LocalDateTime.now());
        BigDecimal calculatedInterest = FinancialCalculationUtil.calculateSimpleInterest(
                originalPrincipalAmount,  // Interest on 500, not 497.5
                tripFinancial.getInterestRate(),
                daysUsed
        );

        // If interest is provided in request, use it; otherwise use calculated
        BigDecimal interestAmount = request.getInterestAmount() != null ?
                request.getInterestAmount() : calculatedInterest;

        BigDecimal totalRepaymentAmount = originalPrincipalAmount.add(interestAmount);

        Wallet fromWallet = walletRepository.findByUserIdWithLock(request.getFromUserId())
                .orElseThrow(() -> new WalletNotFoundException("Payer wallet not found for user: " + request.getFromUserId()));

        Wallet toWallet = walletRepository.findByUserIdWithLock(request.getToUserId())
                .orElseThrow(() -> new WalletNotFoundException("Lender wallet not found for user: " + request.getToUserId()));

        validateWalletStatus(fromWallet);
        validateWalletStatus(toWallet);

        if (!fromWallet.getCurrencyCode().equals(toWallet.getCurrencyCode())) {
            throw new InvalidTransactionException("Currency mismatch between wallets");
        }

        BigDecimal fromBalance = getCurrentBalance(fromWallet.getId());
        BigDecimal fromNewBalance = fromBalance.subtract(totalRepaymentAmount);

        String description = String.format("Repayment for trip %d (Contract: %d) - Original principal: %s, Net received by transporter: %s, Platform fee: %s, Interest calculated on original %s: %s (%d days). Total repayment: %s %s",
                request.getTripId(), request.getContractId(),
                originalPrincipalAmount, tripFinancial.getNetAmountToTransporter(), tripFinancial.getPlatformFeeAmount(),
                originalPrincipalAmount, interestAmount, daysUsed, totalRepaymentAmount, fromWallet.getCurrencyCode());

        Transaction transaction = Transaction.builder()
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .description(description)
                .tripId(request.getTripId())
                .contractId(request.getContractId())
                .transactionPurpose("REPAYMENT")
                .grossAmount(totalRepaymentAmount)
                .platformFeeAmount(BigDecimal.ZERO)  // No platform fee on repayment
                .netAmount(totalRepaymentAmount)
                .createdByUserId(enteredByUserId)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .actualTransferDate(request.getActualTransferDate())
                .build();

        transaction = transactionRepository.save(transaction);

        // Entry 1: Debit from payer wallet (total repayment amount: principal + interest)
        TransactionEntry debitEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(fromWallet.getId())
                .entryType("DEBIT")
                .amount(totalRepaymentAmount)
                .balanceAfter(fromNewBalance)
                .entrySequence((short) 1)
                .build();

        debitEntry = entryRepository.save(debitEntry);

        // Entry 2: Credit to lender wallet (total repayment amount: principal + interest)
        BigDecimal toBalance = getCurrentBalance(toWallet.getId());
        BigDecimal toNewBalance = toBalance.add(totalRepaymentAmount);

        TransactionEntry creditEntry = TransactionEntry.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(toWallet.getId())
                .entryType("CREDIT")
                .amount(totalRepaymentAmount)
                .balanceAfter(toNewBalance)
                .entrySequence((short) 2)
                .build();

        creditEntry = entryRepository.save(creditEntry);

        // Update TripFinancial record
        tripFinancial.setRepaymentTransactionId(transaction.getTransactionId());
        tripFinancial.setRepaymentDate(LocalDateTime.now());
        tripFinancial.setDaysUsed((int) daysUsed);
        tripFinancial.setCalculatedInterest(calculatedInterest);
        tripFinancial.setTotalRepaymentAmount(totalRepaymentAmount);
        tripFinancial.setPrincipalRepaid(originalPrincipalAmount);
        tripFinancial.setInterestRepaid(interestAmount);
        tripFinancial.setStatus("REPAID");
        tripFinancialRepository.save(tripFinancial);

        log.info("TripFinancial updated: Interest calculated on ORIGINAL amount {} for {} days = {}. Total repayment: {}",
                originalPrincipalAmount, daysUsed, calculatedInterest, totalRepaymentAmount);

        ManualTransferRequest manualRequest = ManualTransferRequest.builder()
                .transactionId(transaction.getTransactionId())
                .requestType("TRANSFER")
                .fromUserId(request.getFromUserId())
                .toUserId(request.getToUserId())
                .amount(totalRepaymentAmount)
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks() != null ? request.getRemarks() :
                        String.format("Repayment for trip %d - Original Principal: %s (Net to transporter: %s), Interest on original: %s, Total: %s",
                                request.getTripId(), originalPrincipalAmount, tripFinancial.getNetAmountToTransporter(),
                                interestAmount, totalRepaymentAmount))
                .enteredByUserId(enteredByUserId)
                .enteredAt(LocalDateTime.now())
                .build();

        manualRequest = manualRequestRepository.save(manualRequest);

        if (request.getProofImageBase64() != null && !request.getProofImageBase64().isEmpty()) {
            saveDocument(transaction.getTransactionId(), request.getProofImageBase64(),
                    request.getProofImageFileName(), request.getProofImageMimeType());
        }

        log.info("Repayment transfer processed successfully. Transaction ID: {}. Interest calculated on ORIGINAL amount, not net.",
                transaction.getTransactionId());

        return mapToTransactionResponseDTO(transaction, Arrays.asList(debitEntry, creditEntry), manualRequest);
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
        if (base64Data == null || base64Data.trim().isEmpty()) {
            log.warn("Skipping document save - base64Data is empty for transaction: {}", transactionId);
            return;
        }

        try {
            // Validate Base64 format before decoding
            String cleanBase64 = base64Data.trim();

            // Remove data URL prefix if present (e.g., "data:image/png;base64,")
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }

            // Try to decode - will throw IllegalArgumentException if invalid
            byte[] fileData = Base64.getDecoder().decode(cleanBase64);

            if (fileData.length == 0) {
                log.warn("Decoded file data is empty for transaction: {}", transactionId);
                return;
            }

            TransactionDocument document = TransactionDocument.builder()
                    .transactionId(transactionId)
                    .documentType("PROOF_OF_PAYMENT")
                    .fileName(fileName != null && !fileName.trim().isEmpty() ? fileName : "document.jpg")
                    .mimeType(mimeType != null && !mimeType.trim().isEmpty() ? mimeType : "image/jpeg")
                    .fileData(fileData)
                    .fileSize(fileData.length)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            documentRepository.save(document);
            log.info("Document saved successfully for transaction: {} (size: {} bytes)", transactionId, fileData.length);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 format for transaction: {} - Error: {}", transactionId, e.getMessage());
            throw new InvalidTransactionException("Invalid proof image format. Please provide valid Base64 encoded image data.");
        } catch (Exception e) {
            log.error("Unexpected error saving document for transaction: {}", transactionId, e);
            throw new InvalidTransactionException("Failed to save proof image: " + e.getMessage());
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
