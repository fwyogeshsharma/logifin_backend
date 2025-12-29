package com.logifin.service;

import com.logifin.dto.*;
import com.logifin.entity.TripFinanceProposal.ProposalStatus;

import java.util.List;

/**
 * Service interface for managing Trip Finance Proposals (Lender Interests).
 * Handles lender interest in financing trips and transporter's accept/reject actions.
 */
public interface TripFinanceProposalService {

    // ==================== Lender Operations ====================

    /**
     * Lender marks interest in financing multiple trips (batch operation)
     * @param lenderId ID of the lender
     * @param request Request containing multiple trip IDs and contract ID
     * @return Batch response with success/failure for each trip
     */
    BatchFinanceInterestResponse markInterestInMultipleTrips(Long lenderId, CreateFinanceInterestRequest request);

    /**
     * Get all interests marked by a lender
     * @param lenderId ID of the lender
     * @param status Optional status filter (PENDING, ACCEPTED, REJECTED, WITHDRAWN)
     * @return List of finance interests from lender's perspective
     */
    List<FinanceInterestForLenderDTO> getMyInterests(Long lenderId, ProposalStatus status);

    /**
     * Get a specific interest by ID (for lender)
     * @param lenderId ID of the lender
     * @param interestId ID of the interest
     * @return Finance interest details
     */
    FinanceInterestForLenderDTO getMyInterestById(Long lenderId, Long interestId);

    /**
     * Withdraw a pending interest
     * @param lenderId ID of the lender
     * @param interestId ID of the interest to withdraw
     */
    void withdrawInterest(Long lenderId, Long interestId);

    // ==================== Transporter Operations ====================

    /**
     * Get all interests for a specific trip (for transporter)
     * @param transporterId ID of the transporter
     * @param tripId ID of the trip
     * @return List of finance interests from transporter's perspective
     */
    List<FinanceInterestForTransporterDTO> getInterestsForMyTrip(Long transporterId, Long tripId);

    /**
     * Accept a lender's interest (auto-rejects other pending interests for the trip)
     * @param transporterId ID of the transporter
     * @param interestId ID of the interest to accept
     */
    void acceptLender(Long transporterId, Long interestId);

    /**
     * Reject a lender's interest
     * @param transporterId ID of the transporter
     * @param interestId ID of the interest to reject
     */
    void rejectLender(Long transporterId, Long interestId);

    /**
     * Get all interests for all trips of a transporter
     * @param transporterId ID of the transporter
     * @return List of all finance interests
     */
    List<FinanceInterestForTransporterDTO> getAllInterestsForMyTrips(Long transporterId);
}
