package com.logifin.repository;

import com.logifin.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserIdWithLock(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.userId = :userId AND w.status = 'ACTIVE'")
    boolean isWalletActive(@Param("userId") Long userId);
}
