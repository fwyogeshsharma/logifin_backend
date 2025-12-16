package com.logifin.repository;

import com.logifin.entity.TransactionEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long> {

    List<TransactionEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    Page<TransactionEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    List<TransactionEntry> findByTransactionId(UUID transactionId);

    @Query("SELECT te FROM TransactionEntry te WHERE te.walletId = :walletId " +
           "AND te.createdAt BETWEEN :startDate AND :endDate ORDER BY te.createdAt DESC")
    List<TransactionEntry> findWalletEntriesByDateRange(@Param("walletId") Long walletId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(CASE WHEN te.entryType = 'CREDIT' THEN te.amount ELSE 0 END) - " +
           "SUM(CASE WHEN te.entryType = 'DEBIT' THEN te.amount ELSE 0 END), 0) " +
           "FROM TransactionEntry te WHERE te.walletId = :walletId")
    BigDecimal calculateWalletBalance(@Param("walletId") Long walletId);

    // Spring Data JPA method naming - 'First' keyword automatically limits to 1 result
    Optional<TransactionEntry> findFirstByWalletIdOrderByIdDesc(Long walletId);

    // Native SQL query required for LIMIT syntax
    @Query(value = "SELECT COALESCE(balance_after, 0) FROM transaction_entries " +
           "WHERE wallet_id = :walletId ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<BigDecimal> getLatestBalanceSnapshot(@Param("walletId") Long walletId);
}
