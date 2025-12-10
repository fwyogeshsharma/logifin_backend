package com.logifin.service;

import com.logifin.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Trip Bid management operations.
 */
public interface TripBidService {

    // ==================== CRUD Operations ====================

    /**
     * Create a new bid on a trip
     * @param requestDTO Bid creation request
     * @param lenderId ID of the lender creating the bid
     * @return Created bid response
     */
    TripBidResponseDTO createBid(TripBidRequestDTO requestDTO, Long lenderId);

    /**
     * Get bid by ID
     * @param bidId Bid ID
     * @return Bid response
     */
    TripBidResponseDTO getBidById(Long bidId);

    /**
     * Update an existing bid (only if PENDING)
     * @param bidId Bid ID to update
     * @param requestDTO Update request
     * @param lenderId ID of the lender updating
     * @return Updated bid response
     */
    TripBidResponseDTO updateBid(Long bidId, TripBidRequestDTO requestDTO, Long lenderId);

    /**
     * Cancel a bid (lender cancels their own bid)
     * @param bidId Bid ID to cancel
     * @param lenderId ID of the lender cancelling
     */
    void cancelBid(Long bidId, Long lenderId);

    // ==================== Bid Actions (Transporter) ====================

    /**
     * Accept a bid (transporter accepts)
     * @param bidId Bid ID to accept
     * @param transporterId ID of the transporter accepting
     * @return Updated bid response
     */
    TripBidResponseDTO acceptBid(Long bidId, Long transporterId);

    /**
     * Reject a bid (transporter rejects)
     * @param bidId Bid ID to reject
     * @param actionDTO Action details with rejection reason
     * @param transporterId ID of the transporter rejecting
     * @return Updated bid response
     */
    TripBidResponseDTO rejectBid(Long bidId, TripBidActionDTO actionDTO, Long transporterId);

    /**
     * Make a counter offer on a bid (transporter counters)
     * @param bidId Bid ID to counter
     * @param counterOfferDTO Counter offer details
     * @param transporterId ID of the transporter making counter offer
     * @return Updated bid response
     */
    TripBidResponseDTO counterBid(Long bidId, TripBidCounterOfferDTO counterOfferDTO, Long transporterId);

    // ==================== Bid Actions (Lender) ====================

    /**
     * Accept counter offer (lender accepts transporter's counter)
     * @param bidId Bid ID with counter offer
     * @param lenderId ID of the lender accepting counter
     * @return Updated bid response
     */
    TripBidResponseDTO acceptCounterOffer(Long bidId, Long lenderId);

    /**
     * Reject counter offer (lender rejects transporter's counter)
     * @param bidId Bid ID with counter offer
     * @param lenderId ID of the lender rejecting counter
     * @return Updated bid response
     */
    TripBidResponseDTO rejectCounterOffer(Long bidId, Long lenderId);

    // ==================== Query Operations ====================

    /**
     * Get all bids for a trip
     * @param tripId Trip ID
     * @param pageable Pagination info
     * @return Paginated bid responses
     */
    PagedResponse<TripBidResponseDTO> getBidsForTrip(Long tripId, Pageable pageable);

    /**
     * Get active bids for a trip (PENDING or COUNTERED)
     * @param tripId Trip ID
     * @return List of active bids
     */
    List<TripBidResponseDTO> getActiveBidsForTrip(Long tripId);

    /**
     * Get accepted bid for a trip
     * @param tripId Trip ID
     * @return Accepted bid if exists
     */
    TripBidResponseDTO getAcceptedBidForTrip(Long tripId);

    /**
     * Get all bids by a lender
     * @param lenderId Lender ID
     * @param pageable Pagination info
     * @return Paginated bid responses
     */
    PagedResponse<TripBidResponseDTO> getBidsByLender(Long lenderId, Pageable pageable);

    /**
     * Get bids by lender filtered by status
     * @param lenderId Lender ID
     * @param status Bid status filter
     * @param pageable Pagination info
     * @return Paginated bid responses
     */
    PagedResponse<TripBidResponseDTO> getBidsByLenderAndStatus(Long lenderId, String status, Pageable pageable);

    /**
     * Get all bids by a company
     * @param companyId Company ID
     * @param pageable Pagination info
     * @return Paginated bid responses
     */
    PagedResponse<TripBidResponseDTO> getBidsByCompany(Long companyId, Pageable pageable);

    /**
     * Search bids with criteria
     * @param criteria Search criteria
     * @param pageable Pagination info
     * @return Paginated search results
     */
    PagedResponse<TripBidResponseDTO> searchBids(TripBidSearchCriteria criteria, Pageable pageable);

    // ==================== Statistics ====================

    /**
     * Get bid statistics
     * @return Bid statistics DTO
     */
    TripBidStatisticsDTO getBidStatistics();

    /**
     * Get bid statistics for a specific trip
     * @param tripId Trip ID
     * @return Bid statistics for the trip
     */
    TripBidStatisticsDTO getBidStatisticsForTrip(Long tripId);

    /**
     * Get bid statistics for a lender
     * @param lenderId Lender ID
     * @return Bid statistics for the lender
     */
    TripBidStatisticsDTO getBidStatisticsForLender(Long lenderId);

    // ==================== Expiry Management ====================

    /**
     * Process and update expired bids
     * @return Number of bids marked as expired
     */
    int processExpiredBids();

    // ==================== Validation ====================

    /**
     * Check if lender has active bid on trip
     * @param tripId Trip ID
     * @param lenderId Lender ID
     * @return true if active bid exists
     */
    boolean hasActiveBidOnTrip(Long tripId, Long lenderId);
}
