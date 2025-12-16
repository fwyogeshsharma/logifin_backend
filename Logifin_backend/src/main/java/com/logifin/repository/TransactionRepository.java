package com.logifin.repository;

import com.logifin.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByCreatedByUserId(Long userId);

    Page<Transaction> findByCreatedByUserId(Long userId, Pageable pageable);

    List<Transaction> findByTransactionType(String transactionType);

    List<Transaction> findByStatus(String status);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :type AND t.status = :status")
    Page<Transaction> findByTypeAndStatus(@Param("type") String type,
                                         @Param("status") String status,
                                         Pageable pageable);
}
