package com.logifin.repository;

import com.logifin.entity.ManualTransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManualTransferRequestRepository extends JpaRepository<ManualTransferRequest, Long> {

    Optional<ManualTransferRequest> findByTransactionId(UUID transactionId);

    List<ManualTransferRequest> findByFromUserId(Long fromUserId);

    List<ManualTransferRequest> findByToUserId(Long toUserId);

    Page<ManualTransferRequest> findByFromUserId(Long fromUserId, Pageable pageable);

    Page<ManualTransferRequest> findByToUserId(Long toUserId, Pageable pageable);

    List<ManualTransferRequest> findByRequestType(String requestType);

    @Query("SELECT m FROM ManualTransferRequest m WHERE m.fromUserId = :userId OR m.toUserId = :userId " +
           "ORDER BY m.enteredAt DESC")
    List<ManualTransferRequest> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM ManualTransferRequest m WHERE m.fromUserId = :userId OR m.toUserId = :userId " +
           "ORDER BY m.enteredAt DESC")
    Page<ManualTransferRequest> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM ManualTransferRequest m WHERE m.enteredAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.enteredAt DESC")
    List<ManualTransferRequest> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM ManualTransferRequest m WHERE (m.fromUserId = :userId OR m.toUserId = :userId) " +
           "AND m.enteredAt BETWEEN :startDate AND :endDate ORDER BY m.enteredAt DESC")
    List<ManualTransferRequest> findByUserAndDateRange(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);
}
