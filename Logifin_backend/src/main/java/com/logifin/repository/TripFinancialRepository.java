package com.logifin.repository;

import com.logifin.entity.TripFinancial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripFinancialRepository extends JpaRepository<TripFinancial, Long> {

    /**
     * Find trip financial by trip ID
     */
    Optional<TripFinancial> findByTripId(Long tripId);

    /**
     * Find all trip financials by contract ID
     */
    List<TripFinancial> findByContractId(Long contractId);

    /**
     * Find trip financial by financing transaction ID
     */
    Optional<TripFinancial> findByFinancingTransactionId(UUID financingTransactionId);

    /**
     * Find all trip financials by status
     */
    List<TripFinancial> findByStatus(String status);

    /**
     * Find all financed trips (not yet repaid)
     */
    @Query("SELECT tf FROM TripFinancial tf WHERE tf.status = 'FINANCED'")
    List<TripFinancial> findAllFinancedTrips();

    /**
     * Find all repaid trips
     */
    @Query("SELECT tf FROM TripFinancial tf WHERE tf.status = 'REPAID'")
    List<TripFinancial> findAllRepaidTrips();

    /**
     * Get original principal amount for a trip (for interest calculation)
     */
    @Query("SELECT tf.originalPrincipalAmount FROM TripFinancial tf WHERE tf.tripId = :tripId")
    Optional<java.math.BigDecimal> getOriginalPrincipalAmountByTripId(@Param("tripId") Long tripId);

    /**
     * Check if trip has been financed
     */
    boolean existsByTripId(Long tripId);
}
