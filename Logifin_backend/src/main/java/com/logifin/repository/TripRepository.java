package com.logifin.repository;

import com.logifin.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Trip entity with JPA Specification support for dynamic filtering.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

    /**
     * Find trips by transporter name
     */
    Page<Trip> findByTransporterContainingIgnoreCase(String transporter, Pageable pageable);

    /**
     * Find trips by status
     */
    Page<Trip> findByStatus(Trip.TripStatus status, Pageable pageable);

    /**
     * Find trips by company ID
     */
    Page<Trip> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Find trips by created by user ID
     */
    Page<Trip> findByCreatedByUserId(Long userId, Pageable pageable);

    /**
     * Search trips by keyword across multiple fields
     */
    @Query("SELECT t FROM Trip t WHERE " +
           "LOWER(t.pickup) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.destination) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.sender) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.receiver) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.transporter) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Trip> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find trips by date range
     */
    @Query("SELECT t FROM Trip t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    Page<Trip> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * Find trips by loan amount range
     */
    @Query("SELECT t FROM Trip t WHERE t.loanAmount BETWEEN :minAmount AND :maxAmount")
    Page<Trip> findByLoanAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                        @Param("maxAmount") BigDecimal maxAmount,
                                        Pageable pageable);

    // Statistics queries

    /**
     * Count trips by status
     */
    long countByStatus(Trip.TripStatus status);

    /**
     * Count trips created after a specific date
     */
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt >= :date")
    long countByCreatedAtAfter(@Param("date") LocalDateTime date);

    /**
     * Get total loan amount
     */
    @Query("SELECT COALESCE(SUM(t.loanAmount), 0) FROM Trip t")
    BigDecimal getTotalLoanAmount();

    /**
     * Get total loan amount by status
     */
    @Query("SELECT COALESCE(SUM(t.loanAmount), 0) FROM Trip t WHERE t.status = :status")
    BigDecimal getTotalLoanAmountByStatus(@Param("status") Trip.TripStatus status);

    /**
     * Get average loan amount
     */
    @Query("SELECT COALESCE(AVG(t.loanAmount), 0) FROM Trip t")
    BigDecimal getAverageLoanAmount();

    /**
     * Get average interest rate
     */
    @Query("SELECT COALESCE(AVG(t.interestRate), 0) FROM Trip t")
    BigDecimal getAverageInterestRate();

    /**
     * Get average maturity days
     */
    @Query("SELECT COALESCE(AVG(t.maturityDays), 0) FROM Trip t")
    Double getAverageMaturityDays();

    /**
     * Get total distance
     */
    @Query("SELECT COALESCE(SUM(t.distanceKm), 0) FROM Trip t WHERE t.distanceKm IS NOT NULL")
    BigDecimal getTotalDistance();

    /**
     * Get total weight
     */
    @Query("SELECT COALESCE(SUM(t.weightKg), 0) FROM Trip t WHERE t.weightKg IS NOT NULL")
    BigDecimal getTotalWeight();

    /**
     * Get top pickup locations
     */
    @Query("SELECT t.pickup, COUNT(t) FROM Trip t GROUP BY t.pickup ORDER BY COUNT(t) DESC")
    List<Object[]> getTopPickupLocations(Pageable pageable);

    /**
     * Get top destinations
     */
    @Query("SELECT t.destination, COUNT(t) FROM Trip t GROUP BY t.destination ORDER BY COUNT(t) DESC")
    List<Object[]> getTopDestinations(Pageable pageable);

    /**
     * Get top transporters
     */
    @Query("SELECT t.transporter, COUNT(t) FROM Trip t GROUP BY t.transporter ORDER BY COUNT(t) DESC")
    List<Object[]> getTopTransporters(Pageable pageable);

    /**
     * Get trip count by load type
     */
    @Query("SELECT t.loadType, COUNT(t) FROM Trip t WHERE t.loadType IS NOT NULL GROUP BY t.loadType ORDER BY COUNT(t) DESC")
    List<Object[]> getTripCountByLoadType();

    /**
     * Find all trips for export
     */
    @Query("SELECT t FROM Trip t")
    List<Trip> findAllForExport();
}
