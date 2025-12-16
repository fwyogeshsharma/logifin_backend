package com.logifin.repository;

import com.logifin.entity.Transaction;
import com.logifin.entity.TransactionEntry;
import com.logifin.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TransactionEntryRepository Tests")
class TransactionEntryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionEntryRepository entryRepository;

    private Wallet testWallet;
    private Transaction testTransaction;
    private TransactionEntry testEntry;

    @BeforeEach
    void setUp() {
        testWallet = Wallet.builder()
                .userId(1L)
                .currencyCode("INR")
                .status("ACTIVE")
                .build();
        testWallet = entityManager.persist(testWallet);

        testTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .transactionType("MANUAL_CREDIT")
                .status("COMPLETED")
                .description("Test transaction")
                .createdByUserId(1L)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
        testTransaction = entityManager.persist(testTransaction);

        testEntry = TransactionEntry.builder()
                .transactionId(testTransaction.getTransactionId())
                .walletId(testWallet.getId())
                .entryType("CREDIT")
                .amount(new BigDecimal("1000.00"))
                .balanceAfter(new BigDecimal("1000.00"))
                .entrySequence((short) 1)
                .build();

        entityManager.flush();
    }

    @Test
    @DisplayName("Should save transaction entry successfully")
    void shouldSaveTransactionEntrySuccessfully() {
        TransactionEntry saved = entryRepository.save(testEntry);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWalletId()).isEqualTo(testWallet.getId());
        assertThat(saved.getEntryType()).isEqualTo("CREDIT");
        assertThat(saved.getAmount()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Should find entries by wallet ID ordered by created date")
    void shouldFindEntriesByWalletIdOrderedByCreatedDate() {
        entryRepository.save(testEntry);

        TransactionEntry entry2 = TransactionEntry.builder()
                .transactionId(testTransaction.getTransactionId())
                .walletId(testWallet.getId())
                .entryType("DEBIT")
                .amount(new BigDecimal("300.00"))
                .balanceAfter(new BigDecimal("700.00"))
                .entrySequence((short) 2)
                .build();
        entryRepository.save(entry2);

        List<TransactionEntry> entries = entryRepository.findByWalletIdOrderByCreatedAtDesc(testWallet.getId());

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getEntryType()).isEqualTo("DEBIT");
        assertThat(entries.get(1).getEntryType()).isEqualTo("CREDIT");
    }

    @Test
    @DisplayName("Should find paginated entries by wallet ID")
    void shouldFindPaginatedEntriesByWalletId() {
        entryRepository.save(testEntry);

        Page<TransactionEntry> page = entryRepository.findByWalletIdOrderByCreatedAtDesc(
                testWallet.getId(), PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find entries by transaction ID")
    void shouldFindEntriesByTransactionId() {
        entryRepository.save(testEntry);

        List<TransactionEntry> entries = entryRepository.findByTransactionId(testTransaction.getTransactionId());

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getTransactionId()).isEqualTo(testTransaction.getTransactionId());
    }

    @Test
    @DisplayName("Should calculate wallet balance correctly")
    void shouldCalculateWalletBalanceCorrectly() {
        entryRepository.save(testEntry);

        TransactionEntry debitEntry = TransactionEntry.builder()
                .transactionId(testTransaction.getTransactionId())
                .walletId(testWallet.getId())
                .entryType("DEBIT")
                .amount(new BigDecimal("300.00"))
                .balanceAfter(new BigDecimal("700.00"))
                .entrySequence((short) 2)
                .build();
        entryRepository.save(debitEntry);

        BigDecimal balance = entryRepository.calculateWalletBalance(testWallet.getId());

        assertThat(balance).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("Should get latest balance snapshot")
    void shouldGetLatestBalanceSnapshot() {
        entryRepository.save(testEntry);

        TransactionEntry entry2 = TransactionEntry.builder()
                .transactionId(testTransaction.getTransactionId())
                .walletId(testWallet.getId())
                .entryType("CREDIT")
                .amount(new BigDecimal("500.00"))
                .balanceAfter(new BigDecimal("1500.00"))
                .entrySequence((short) 2)
                .build();
        entryRepository.save(entry2);

        Optional<BigDecimal> balance = entryRepository.getLatestBalanceSnapshot(testWallet.getId());

        assertThat(balance).isPresent();
        assertThat(balance.get()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Should find wallet entries by date range")
    void shouldFindWalletEntriesByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        entryRepository.save(testEntry);

        TransactionEntry entry2 = TransactionEntry.builder()
                .transactionId(testTransaction.getTransactionId())
                .walletId(testWallet.getId())
                .entryType("DEBIT")
                .amount(new BigDecimal("200.00"))
                .balanceAfter(new BigDecimal("800.00"))
                .entrySequence((short) 2)
                .build();
        entryRepository.save(entry2);

        LocalDateTime fromDate = now.minusHours(1);
        LocalDateTime toDate = now.plusHours(2);

        List<TransactionEntry> entries = entryRepository.findWalletEntriesByDateRange(
                testWallet.getId(), fromDate, toDate);

        assertThat(entries).hasSize(2);
    }

    @Test
    @DisplayName("Should return zero balance for empty wallet")
    void shouldReturnZeroBalanceForEmptyWallet() {
        BigDecimal balance = entryRepository.calculateWalletBalance(999L);

        assertThat(balance).isEqualByComparingTo("0");
    }
}
