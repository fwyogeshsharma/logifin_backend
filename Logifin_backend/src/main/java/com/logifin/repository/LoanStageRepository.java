package com.logifin.repository;

import com.logifin.entity.LoanStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for LoanStage entity - Master data
 */
@Repository
public interface LoanStageRepository extends JpaRepository<LoanStage, Long> {

    /**
     * Find loan stage by stage name
     */
    Optional<LoanStage> findByStageName(String stageName);

    /**
     * Find loan stage by stage order
     */
    Optional<LoanStage> findByStageOrder(Integer stageOrder);

    /**
     * Check if a stage name exists
     */
    boolean existsByStageName(String stageName);

    /**
     * Check if a stage order exists
     */
    boolean existsByStageOrder(Integer stageOrder);
}
