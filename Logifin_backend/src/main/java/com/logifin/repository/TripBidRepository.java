package com.logifin.repository;

import com.logifin.entity.TripBid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TripBid entity with JPA Specification support for dynamic filtering.
 */
@Repository
public interface TripBidRepository extends JpaRepository<TripBid, Long>, JpaSpecificationExecutor<TripBid> {

    // ==================== Basic Queries ====================

    /**
     * Find bids by trip ID
     */
    List<TripBid> findByTripId(Long tripId);

    /**
     * Find bids by trip ID with pagination
     */
    Page<TripBid> findByTripId(Long tripId, Pageable pageable);

    /**
     * Find bids by lender ID
     */
    List<TripBid> findByLenderId(Long lenderId);

    /**
     * Find bids by lender ID with pagination
     */
    Page<TripBid> findByLenderId(Long lenderId, Pageable pageable);

    /**
     * Find bids by company ID
     */
    Page<TripBid> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Find bids by status
     */
    Page<TripBid> findByStatus(TripBid.BidStatus status, Pageable pageable);

    /**
     * Find bids by trip ID and status
     */
    List<TripBid> findByTripIdAndStatus(Long tripId, TripBid.BidStatus status);

    /**
     * Find bids by lender ID and status
     */
    Page<TripBid> findByLenderIdAndStatus(Long lenderId, TripBid.BidStatus status, Pageable pageable);

    /**
     * Check if lender has already placed a pending bid on a trip
     */
    boolean existsByTripIdAndLenderIdAndStatusIn(Long tripId, Long lenderId, List<TripBid.BidStatus> statuses);

    /**
     * Find pending/countered bids for a trip (active bids)
     */
    @Query("SELECT b FROM TripBid b WHERE b.trip.id = :tripId AND b.status IN ('PENDING', 'COUNTERED') ORDER BY b.createdAt DESC")
    List<TripBid> findActiveBidsByTripId(@Param("tripId") Long tripId);

    /**
     * Find first bid for a trip with given status (useful for finding accepted bid)
     */
    Optional<TripBid> findFirstByTripIdAndStatus(Long tripId, TripBid.BidStatus status);

    // ==================== Expiry Management ====================

    /**
     * Find expired bids that need status update
     */
    @Query("SELECT b FROM TripBid b WHERE b.status IN ('PENDING', 'COUNTERED') AND b.expiresAt IS NOT NULL AND b.expiresAt < :now")
    List<TripBid> findExpiredBids(@Param("now") LocalDateTime now);

    /**
     * Update expired bids to EXPIRED status
     */
    @Modifying
    @Query("UPDATE TripBid b SET b.status = 'EXPIRED', b.updatedAt = :now WHERE b.status IN ('PENDING', 'COUNTERED') AND b.expiresAt IS NOT NULL AND b.expiresAt < :now")
    int updateExpiredBids(@Param("now") LocalDateTime now);

    // ==================== Statistics Queries ====================

    /**
     * Count bids by status
     */
    long countByStatus(TripBid.BidStatus status);

    /**
     * Count bids by trip ID
     */
    long countByTripId(Long tripId);

    /**
     * Count bids by lender ID
     */
    long countByLenderId(Long lenderId);

    /**
     * Count bids for a trip by status
     */
    long countByTripIdAndStatus(Long tripId, TripBid.BidStatus status);

    /**
     * Get total bid amount
     */
    @Query("SELECT COALESCE(SUM(b.bidAmount), 0) FROM TripBid b")
    BigDecimal getTotalBidAmount();

    /**
     * Get total accepted bid amount
     */
    @Query("SELECT COALESCE(SUM(b.bidAmount), 0) FROM TripBid b WHERE b.status = 'ACCEPTED'")
    BigDecimal getTotalAcceptedBidAmount();

    /**
     * Get average bid amount
     */
    @Query("SELECT COALESCE(AVG(b.bidAmount), 0) FROM TripBid b")
    BigDecimal getAverageBidAmount();

    /**
     * Get average interest rate
     */
    @Query("SELECT COALESCE(AVG(b.interestRate), 0) FROM TripBid b WHERE b.interestRate IS NOT NULL")
    BigDecimal getAverageInterestRate();

    /**
     * Get average maturity days
     */
    @Query("SELECT COALESCE(AVG(b.maturityDays), 0) FROM TripBid b WHERE b.maturityDays IS NOT NULL")
    Double getAverageMaturityDays();

    /**
     * Count unique lenders
     */
    @Query("SELECT COUNT(DISTINCT b.lender.id) FROM TripBid b")
    Long countUniqueLenders();

    /**
     * Count trips with bids
     */
    @Query("SELECT COUNT(DISTINCT b.trip.id) FROM TripBid b")
    Long countTripsWithBids();

    /**
     * Get bid count by status for a trip
     */
    @Query("SELECT b.status, COUNT(b) FROM TripBid b WHERE b.trip.id = :tripId GROUP BY b.status")
    List<Object[]> getBidCountByStatusForTrip(@Param("tripId") Long tripId);

    /**
     * Get top lenders by bid count
     */
    @Query("SELECT b.lender.id, b.lender.firstName, b.lender.lastName, COUNT(b) FROM TripBid b GROUP BY b.lender.id, b.lender.firstName, b.lender.lastName ORDER BY COUNT(b) DESC")
    List<Object[]> getTopLendersByBidCount(Pageable pageable);

    /**
     * Get top lenders by accepted bid amount
     */
    @Query("SELECT b.lender.id, b.lender.firstName, b.lender.lastName, SUM(b.bidAmount) FROM TripBid b WHERE b.status = 'ACCEPTED' GROUP BY b.lender.id, b.lender.firstName, b.lender.lastName ORDER BY SUM(b.bidAmount) DESC")
    List<Object[]> getTopLendersByAcceptedAmount(Pageable pageable);

    // ==================== Search Queries ====================

    /**
     * Search bids by keyword (lender name, company name, notes)
     */
    @Query("SELECT b FROM TripBid b WHERE " +
           "LOWER(CONCAT(b.lender.firstName, ' ', b.lender.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.company.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<TripBid> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find bids by amount range
     */
    @Query("SELECT b FROM TripBid b WHERE b.bidAmount BETWEEN :minAmount AND :maxAmount")
    Page<TripBid> findByBidAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                          @Param("maxAmount") BigDecimal maxAmount,
                                          Pageable pageable);

    /**
     * Find bids by date range
     */
    @Query("SELECT b FROM TripBid b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Page<TripBid> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
}
