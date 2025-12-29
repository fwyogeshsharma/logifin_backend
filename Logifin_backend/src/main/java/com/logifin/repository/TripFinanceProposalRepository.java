package com.logifin.repository;

import com.logifin.entity.TripFinanceProposal;
import com.logifin.entity.TripFinanceProposal.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TripFinanceProposal entity.
 * Manages lender interests in financing trips.
 */
@Repository
public interface TripFinanceProposalRepository extends JpaRepository<TripFinanceProposal, Long> {

    /**
     * Find all proposals for a specific trip
     * Used by transporters to view all lenders interested in their trip
     */
    List<TripFinanceProposal> findByTripId(Long tripId);

    /**
     * Find all proposals for a specific trip with a given status
     */
    List<TripFinanceProposal> findByTripIdAndStatus(Long tripId, ProposalStatus status);

    /**
     * Find all proposals made by a specific lender
     * Used by lenders to view trips they've shown interest in
     */
    List<TripFinanceProposal> findByLenderId(Long lenderId);

    /**
     * Find all proposals made by a specific lender with a given status
     */
    List<TripFinanceProposal> findByLenderIdAndStatus(Long lenderId, ProposalStatus status);

    /**
     * Find a specific proposal by trip, lender, and contract
     */
    Optional<TripFinanceProposal> findByTripIdAndLenderIdAndContractId(
            Long tripId, Long lenderId, Long contractId);

    /**
     * Check if a proposal exists for the given trip, lender, and contract combination
     * Used to prevent duplicate interests
     */
    boolean existsByTripIdAndLenderIdAndContractId(
            Long tripId, Long lenderId, Long contractId);

    /**
     * Find existing proposals for a lender across multiple trips
     * Used for batch operations to check for duplicates
     */
    @Query("SELECT p FROM TripFinanceProposal p WHERE p.trip.id IN :tripIds AND p.lender.id = :lenderId")
    List<TripFinanceProposal> findByTripIdInAndLenderId(
            @Param("tripIds") List<Long> tripIds,
            @Param("lenderId") Long lenderId);

    /**
     * Find a proposal by ID and lender ID
     * Used to verify ownership before withdrawal
     */
    Optional<TripFinanceProposal> findByIdAndLenderId(Long id, Long lenderId);

    /**
     * Find a proposal by ID and trip's transporter (createdByUser of trip)
     * Used to verify ownership before accept/reject
     */
    @Query("SELECT p FROM TripFinanceProposal p WHERE p.id = :proposalId AND p.trip.createdByUser.id = :transporterId")
    Optional<TripFinanceProposal> findByIdAndTransporterId(
            @Param("proposalId") Long proposalId,
            @Param("transporterId") Long transporterId);

    /**
     * Update all pending proposals for a trip to REJECTED status
     * Used when transporter accepts one proposal
     */
    @Modifying
    @Query("UPDATE TripFinanceProposal p SET p.status = 'REJECTED', p.respondedAt = CURRENT_TIMESTAMP " +
           "WHERE p.trip.id = :tripId AND p.status = 'PENDING' AND p.id != :acceptedProposalId")
    int rejectOtherPendingProposals(
            @Param("tripId") Long tripId,
            @Param("acceptedProposalId") Long acceptedProposalId);

    /**
     * Count proposals for a specific trip by status
     */
    long countByTripIdAndStatus(Long tripId, ProposalStatus status);

    /**
     * Find all proposals for trips belonging to a specific transporter
     */
    @Query("SELECT p FROM TripFinanceProposal p WHERE p.trip.createdByUser.id = :transporterId")
    List<TripFinanceProposal> findAllByTransporterId(@Param("transporterId") Long transporterId);

    /**
     * Find proposals by lender and contract
     * Useful to see all trips a lender is interested in using a specific contract
     */
    List<TripFinanceProposal> findByLenderIdAndContractId(Long lenderId, Long contractId);

    /**
     * Check if a trip has any accepted proposal
     */
    boolean existsByTripIdAndStatus(Long tripId, ProposalStatus status);
}
