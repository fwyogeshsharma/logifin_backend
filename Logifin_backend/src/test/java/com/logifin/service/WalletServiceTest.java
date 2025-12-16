package com.logifin.service;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.*;
import com.logifin.repository.*;
import com.logifin.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Tests")
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEntryRepository entryRepository;

    @Mock
    private ManualTransferRequestRepository manualRequestRepository;

    @Mock
    private TransactionDocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet testWallet;
    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .active(true)
                .build();
        testUser.setId(1L);

        testWallet = Wallet.builder()
                .userId(1L)
                .currencyCode("INR")
                .status("ACTIVE")
                .build();
        testWallet.setId(1L);
        testWallet.setCreatedAt(LocalDateTime.now());
        testWallet.setUpdatedAt(LocalDateTime.now());

        testTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .transactionType("MANUAL_CREDIT")
                .status("COMPLETED")
                .description("Test transaction")
                .createdByUserId(1L)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Wallet Tests")
    class CreateWalletTests {

        @Test
        @DisplayName("Should create wallet successfully")
        void shouldCreateWalletSuccessfully() {
            CreateWalletRequest request = CreateWalletRequest.builder()
                    .userId(1L)
                    .currencyCode("INR")
                    .build();

            when(walletRepository.existsByUserId(1L)).thenReturn(false);
            when(userRepository.existsById(1L)).thenReturn(true);
            when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
            when(entryRepository.getLatestBalanceSnapshot(anyLong())).thenReturn(Optional.of(BigDecimal.ZERO));

            WalletDTO result = walletService.createWallet(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getCurrencyCode()).isEqualTo("INR");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");

            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should throw exception when wallet already exists")
        void shouldThrowExceptionWhenWalletAlreadyExists() {
            CreateWalletRequest request = CreateWalletRequest.builder()
                    .userId(1L)
                    .currencyCode("INR")
                    .build();

            when(walletRepository.existsByUserId(1L)).thenReturn(true);

            assertThatThrownBy(() -> walletService.createWallet(request, 1L))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Wallet already exists for user: 1");

            verify(walletRepository, never()).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            CreateWalletRequest request = CreateWalletRequest.builder()
                    .userId(999L)
                    .currencyCode("INR")
                    .build();

            when(walletRepository.existsByUserId(999L)).thenReturn(false);
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> walletService.createWallet(request, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with ID: 999");

            verify(walletRepository, never()).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("Get Wallet Tests")
    class GetWalletTests {

        @Test
        @DisplayName("Should get wallet by user ID successfully")
        void shouldGetWalletByUserIdSuccessfully() {
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(anyLong()))
                    .thenReturn(Optional.of(new BigDecimal("1000.00")));

            WalletDTO result = walletService.getWalletByUserId(1L);

            assertThat(result).isNotNull();
            assertThat(result.getWalletId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getCurrentBalance()).isEqualByComparingTo("1000.00");

            verify(walletRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("Should throw exception when wallet not found")
        void shouldThrowExceptionWhenWalletNotFound() {
            when(walletRepository.findByUserId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.getWalletByUserId(999L))
                    .isInstanceOf(WalletNotFoundException.class)
                    .hasMessageContaining("Wallet not found for user: 999");
        }
    }

    @Nested
    @DisplayName("Get Balance Tests")
    class GetBalanceTests {

        @Test
        @DisplayName("Should get wallet balance successfully")
        void shouldGetWalletBalanceSuccessfully() {
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L))
                    .thenReturn(Optional.of(new BigDecimal("5000.00")));

            WalletBalanceDTO result = walletService.getWalletBalance(1L);

            assertThat(result).isNotNull();
            assertThat(result.getWalletId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getAvailableBalance()).isEqualByComparingTo("5000.00");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getAsOfTime()).isNotNull();

            verify(walletRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("Should calculate balance from ledger when snapshot not available")
        void shouldCalculateBalanceFromLedger() {
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L)).thenReturn(Optional.empty());
            when(entryRepository.calculateWalletBalance(1L)).thenReturn(new BigDecimal("2500.00"));

            WalletBalanceDTO result = walletService.getWalletBalance(1L);

            assertThat(result.getAvailableBalance()).isEqualByComparingTo("2500.00");
            verify(entryRepository).calculateWalletBalance(1L);
        }
    }

    @Nested
    @DisplayName("Manual Credit Tests")
    class ManualCreditTests {

        @Test
        @DisplayName("Should process manual credit successfully")
        void shouldProcessManualCreditSuccessfully() {
            ManualCreditRequest request = ManualCreditRequest.builder()
                    .userId(1L)
                    .amount(new BigDecimal("1000.00"))
                    .paymentMethod("UPI")
                    .referenceNumber("UPI123456")
                    .remarks("Test deposit")
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L)).thenReturn(Optional.of(BigDecimal.ZERO));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(entryRepository.save(any(TransactionEntry.class)))
                    .thenAnswer(invocation -> {
                        TransactionEntry entry = invocation.getArgument(0);
                        entry.setId(1L);
                        return entry;
                    });
            when(manualRequestRepository.save(any(ManualTransferRequest.class)))
                    .thenAnswer(invocation -> {
                        ManualTransferRequest req = invocation.getArgument(0);
                        req.setId(1L);
                        return req;
                    });
            when(documentRepository.existsByTransactionId(any(UUID.class))).thenReturn(false);

            TransactionResponseDTO result = walletService.processManualCredit(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("MANUAL_CREDIT");
            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            assertThat(result.getAmount()).isEqualByComparingTo("1000.00");
            assertThat(result.getEntries()).hasSize(1);
            assertThat(result.getEntries().get(0).getEntryType()).isEqualTo("CREDIT");
            assertThat(result.getEntries().get(0).getAmount()).isEqualByComparingTo("1000.00");

            verify(transactionRepository).save(any(Transaction.class));
            verify(entryRepository).save(any(TransactionEntry.class));
            verify(manualRequestRepository).save(any(ManualTransferRequest.class));
        }

        @Test
        @DisplayName("Should process manual credit with actual transfer date")
        void shouldProcessManualCreditWithActualTransferDate() {
            LocalDateTime actualTransferDate = LocalDateTime.now().minusDays(1);

            ManualCreditRequest request = ManualCreditRequest.builder()
                    .userId(1L)
                    .amount(new BigDecimal("1000.00"))
                    .paymentMethod("UPI")
                    .referenceNumber("UPI123456")
                    .remarks("Test deposit")
                    .actualTransferDate(actualTransferDate)
                    .build();

            Transaction transactionWithDate = Transaction.builder()
                    .transactionId(UUID.randomUUID())
                    .transactionType("MANUAL_CREDIT")
                    .status("COMPLETED")
                    .description("Test transaction")
                    .createdByUserId(1L)
                    .createdAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .actualTransferDate(actualTransferDate)
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L)).thenReturn(Optional.of(BigDecimal.ZERO));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionWithDate);
            when(entryRepository.save(any(TransactionEntry.class)))
                    .thenAnswer(invocation -> {
                        TransactionEntry entry = invocation.getArgument(0);
                        entry.setId(1L);
                        return entry;
                    });
            when(manualRequestRepository.save(any(ManualTransferRequest.class)))
                    .thenAnswer(invocation -> {
                        ManualTransferRequest req = invocation.getArgument(0);
                        req.setId(1L);
                        return req;
                    });
            when(documentRepository.existsByTransactionId(any(UUID.class))).thenReturn(false);

            TransactionResponseDTO result = walletService.processManualCredit(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getActualTransferDate()).isEqualTo(actualTransferDate);

            verify(transactionRepository).save(argThat(transaction ->
                    transaction.getActualTransferDate() != null &&
                    transaction.getActualTransferDate().equals(actualTransferDate)
            ));
        }

        @Test
        @DisplayName("Should throw exception when wallet suspended")
        void shouldThrowExceptionWhenWalletSuspended() {
            testWallet.setStatus("SUSPENDED");

            ManualCreditRequest request = ManualCreditRequest.builder()
                    .userId(1L)
                    .amount(new BigDecimal("1000.00"))
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));

            assertThatThrownBy(() -> walletService.processManualCredit(request, 1L))
                    .isInstanceOf(WalletSuspendedException.class)
                    .hasMessageContaining("Wallet is suspended for user: 1");

            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Manual Debit Tests")
    class ManualDebitTests {

        @Test
        @DisplayName("Should process manual debit successfully")
        void shouldProcessManualDebitSuccessfully() {
            ManualDebitRequest request = ManualDebitRequest.builder()
                    .userId(1L)
                    .amount(new BigDecimal("500.00"))
                    .paymentMethod("BANK_TRANSFER")
                    .referenceNumber("TXN123456")
                    .remarks("Test withdrawal")
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L))
                    .thenReturn(Optional.of(new BigDecimal("1000.00")));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(entryRepository.save(any(TransactionEntry.class)))
                    .thenAnswer(invocation -> {
                        TransactionEntry entry = invocation.getArgument(0);
                        entry.setId(1L);
                        return entry;
                    });
            when(manualRequestRepository.save(any(ManualTransferRequest.class)))
                    .thenAnswer(invocation -> {
                        ManualTransferRequest req = invocation.getArgument(0);
                        req.setId(1L);
                        return req;
                    });
            when(documentRepository.existsByTransactionId(any(UUID.class))).thenReturn(false);

            TransactionResponseDTO result = walletService.processManualDebit(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("MANUAL_DEBIT");
            assertThat(result.getAmount()).isEqualByComparingTo("500.00");
            assertThat(result.getEntries().get(0).getEntryType()).isEqualTo("DEBIT");

            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should allow negative balance (borrowing)")
        void shouldAllowNegativeBalance() {
            ManualDebitRequest request = ManualDebitRequest.builder()
                    .userId(1L)
                    .amount(new BigDecimal("2000.00"))
                    .paymentMethod("BANK_TRANSFER")
                    .referenceNumber("TXN999")
                    .remarks("Test borrowing")
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L))
                    .thenReturn(Optional.of(new BigDecimal("500.00")));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(entryRepository.save(any(TransactionEntry.class)))
                    .thenAnswer(invocation -> {
                        TransactionEntry entry = invocation.getArgument(0);
                        entry.setId(1L);
                        return entry;
                    });
            when(manualRequestRepository.save(any(ManualTransferRequest.class)))
                    .thenAnswer(invocation -> {
                        ManualTransferRequest req = invocation.getArgument(0);
                        req.setId(1L);
                        return req;
                    });
            when(documentRepository.existsByTransactionId(any(UUID.class))).thenReturn(false);

            TransactionResponseDTO result = walletService.processManualDebit(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("MANUAL_DEBIT");
            assertThat(result.getAmount()).isEqualByComparingTo("2000.00");
            assertThat(result.getEntries().get(0).getBalanceAfter()).isEqualByComparingTo("-1500.00");
            assertThat(result.getDescription()).contains("Borrowing");

            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {

        private Wallet toWallet;

        @BeforeEach
        void setUp() {
            toWallet = Wallet.builder()
                    .userId(2L)
                    .currencyCode("INR")
                    .status("ACTIVE")
                    .build();
            toWallet.setId(2L);
        }

        @Test
        @DisplayName("Should process transfer successfully")
        void shouldProcessTransferSuccessfully() {
            TransferRequest request = TransferRequest.builder()
                    .fromUserId(1L)
                    .toUserId(2L)
                    .amount(new BigDecimal("300.00"))
                    .paymentMethod("IMPS")
                    .referenceNumber("TXN789")
                    .remarks("Payment for services")
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));
            when(walletRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(toWallet));
            when(entryRepository.getLatestBalanceSnapshot(1L))
                    .thenReturn(Optional.of(new BigDecimal("1000.00")));
            when(entryRepository.getLatestBalanceSnapshot(2L))
                    .thenReturn(Optional.of(new BigDecimal("500.00")));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(entryRepository.save(any(TransactionEntry.class)))
                    .thenAnswer(invocation -> {
                        TransactionEntry entry = invocation.getArgument(0);
                        entry.setId(1L);
                        return entry;
                    });
            when(manualRequestRepository.save(any(ManualTransferRequest.class)))
                    .thenAnswer(invocation -> {
                        ManualTransferRequest req = invocation.getArgument(0);
                        req.setId(1L);
                        return req;
                    });
            when(documentRepository.existsByTransactionId(any(UUID.class))).thenReturn(false);

            TransactionResponseDTO result = walletService.processTransfer(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("TRANSFER");
            assertThat(result.getEntries()).hasSize(2);
            assertThat(result.getEntries().get(0).getEntryType()).isEqualTo("DEBIT");
            assertThat(result.getEntries().get(1).getEntryType()).isEqualTo("CREDIT");

            verify(entryRepository, times(2)).save(any(TransactionEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when transferring to same wallet")
        void shouldThrowExceptionWhenTransferringToSameWallet() {
            TransferRequest request = TransferRequest.builder()
                    .fromUserId(1L)
                    .toUserId(1L)
                    .amount(new BigDecimal("300.00"))
                    .build();

            assertThatThrownBy(() -> walletService.processTransfer(request, 1L))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Cannot transfer to the same wallet");

            verify(walletRepository, never()).findByUserIdWithLock(anyLong());
        }

        @Test
        @DisplayName("Should throw exception for currency mismatch")
        void shouldThrowExceptionForCurrencyMismatch() {
            toWallet.setCurrencyCode("USD");

            TransferRequest request = TransferRequest.builder()
                    .fromUserId(1L)
                    .toUserId(2L)
                    .amount(new BigDecimal("300.00"))
                    .build();

            when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(testWallet));
            when(walletRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(toWallet));

            assertThatThrownBy(() -> walletService.processTransfer(request, 1L))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Currency mismatch between wallets");

            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Wallet Statement Tests")
    class WalletStatementTests {

        @Test
        @DisplayName("Should get wallet statement successfully")
        void shouldGetWalletStatementSuccessfully() {
            LocalDateTime fromDate = LocalDateTime.now().minusDays(30);
            LocalDateTime toDate = LocalDateTime.now();

            TransactionEntry entry1 = TransactionEntry.builder()
                    .transactionId(UUID.randomUUID())
                    .walletId(1L)
                    .entryType("CREDIT")
                    .amount(new BigDecimal("1000.00"))
                    .balanceAfter(new BigDecimal("1000.00"))
                    .build();
            entry1.setId(1L);

            TransactionEntry entry2 = TransactionEntry.builder()
                    .transactionId(UUID.randomUUID())
                    .walletId(1L)
                    .entryType("DEBIT")
                    .amount(new BigDecimal("300.00"))
                    .balanceAfter(new BigDecimal("700.00"))
                    .build();
            entry2.setId(2L);

            List<TransactionEntry> entries = Arrays.asList(entry2, entry1);

            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.findWalletEntriesByDateRange(1L, fromDate, toDate))
                    .thenReturn(entries);

            WalletStatementDTO result = walletService.getWalletStatement(1L, fromDate, toDate);

            assertThat(result).isNotNull();
            assertThat(result.getWalletId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getClosingBalance()).isEqualByComparingTo("700.00");
            assertThat(result.getTotalEntries()).isEqualTo(2);
            assertThat(result.getEntries()).hasSize(2);

            verify(entryRepository).findWalletEntriesByDateRange(1L, fromDate, toDate);
        }
    }

    @Nested
    @DisplayName("Wallet History Tests")
    class WalletHistoryTests {

        @Test
        @DisplayName("Should get paginated wallet history")
        void shouldGetPaginatedWalletHistory() {
            Pageable pageable = PageRequest.of(0, 20);

            TransactionEntry entry = TransactionEntry.builder()
                    .transactionId(UUID.randomUUID())
                    .walletId(1L)
                    .entryType("CREDIT")
                    .amount(new BigDecimal("1000.00"))
                    .balanceAfter(new BigDecimal("1000.00"))
                    .build();
            entry.setId(1L);

            Page<TransactionEntry> entryPage = new PageImpl<>(Collections.singletonList(entry));

            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(entryRepository.findByWalletIdOrderByCreatedAtDesc(1L, pageable))
                    .thenReturn(entryPage);

            Page<TransactionEntryDTO> result = walletService.getWalletHistory(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getWalletId()).isEqualTo(1L);

            verify(entryRepository).findByWalletIdOrderByCreatedAtDesc(1L, pageable);
        }
    }

    @Nested
    @DisplayName("Suspend and Activate Tests")
    class SuspendActivateTests {

        @Test
        @DisplayName("Should suspend wallet successfully")
        void shouldSuspendWalletSuccessfully() {
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
            when(entryRepository.getLatestBalanceSnapshot(anyLong()))
                    .thenReturn(Optional.of(new BigDecimal("1000.00")));

            WalletDTO result = walletService.suspendWallet(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("SUSPENDED");

            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should activate wallet successfully")
        void shouldActivateWalletSuccessfully() {
            testWallet.setStatus("SUSPENDED");

            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
            when(entryRepository.getLatestBalanceSnapshot(anyLong()))
                    .thenReturn(Optional.of(new BigDecimal("1000.00")));

            WalletDTO result = walletService.activateWallet(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("ACTIVE");

            verify(walletRepository).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("Get All Wallets Tests")
    class GetAllWalletsTests {

        @Test
        @DisplayName("Should get all wallets successfully")
        void shouldGetAllWalletsSuccessfully() {
            Wallet wallet2 = Wallet.builder()
                    .userId(2L)
                    .currencyCode("INR")
                    .status("ACTIVE")
                    .build();
            wallet2.setId(2L);

            List<Wallet> wallets = Arrays.asList(testWallet, wallet2);

            when(walletRepository.findAll()).thenReturn(wallets);
            when(entryRepository.getLatestBalanceSnapshot(anyLong()))
                    .thenReturn(Optional.of(new BigDecimal("1000.00")));

            List<WalletDTO> result = walletService.getAllWallets();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getWalletId()).isEqualTo(1L);
            assertThat(result.get(1).getWalletId()).isEqualTo(2L);

            verify(walletRepository).findAll();
        }
    }
}
