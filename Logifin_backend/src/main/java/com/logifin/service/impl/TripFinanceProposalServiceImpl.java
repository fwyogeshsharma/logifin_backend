package com.logifin.service.impl;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.entity.TripFinanceProposal.ProposalStatus;
import com.logifin.exception.BadRequestException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.*;
import com.logifin.service.TripFinanceProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TripFinanceProposalServiceImpl implements TripFinanceProposalService {

    private final TripFinanceProposalRepository proposalRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ContractPartyRepository contractPartyRepository;

    // ==================== Lender Operations ====================

    @Override
    public BatchFinanceInterestResponse markInterestInMultipleTrips(Long lenderId, CreateFinanceInterestRequest request) {
        log.debug("Lender {} marking interest in {} trips", lenderId, request.getTripIds().size());

        // Validate lender exists
        User lender = userRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", lenderId));

        // Process each trip
        List<InterestCreationResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (Long tripId : request.getTripIds()) {
            InterestCreationResult result = processSingleTrip(tripId, lenderId, lender);
            results.add(result);

            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        log.info("Batch processing completed: {} successes, {} failures", successCount, failureCount);

        return BatchFinanceInterestResponse.builder()
                .totalRequested(request.getTripIds().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .build();
    }

    private InterestCreationResult processSingleTrip(Long tripId, Long lenderId, User lender) {
        try {
            // Fetch trip
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

            // Get trip number for response
            String tripNumber = generateTripNumber(trip);

            // Validate trip is available for financing
            validateTripForFinancing(trip);

            // Get transporter and sender from trip
            User transporter = trip.getCreatedByUser();
            User sender = trip.getSender();

            if (transporter == null) {
                return InterestCreationResult.builder()
                        .tripId(tripId)
                        .tripNumber(tripNumber)
                        .success(false)
                        .message("Trip does not have a transporter assigned")
                        .build();
            }

            if (sender == null) {
                return InterestCreationResult.builder()
                        .tripId(tripId)
                        .tripNumber(tripNumber)
                        .success(false)
                        .message("Trip does not have a sender/consigner assigned")
                        .build();
            }

            // Find contract between lender, transporter, and sender
            List<Contract> contracts = contractPartyRepository.findActiveContractsByThreeParties(
                    lenderId, transporter.getId(), sender.getId());

            if (contracts.isEmpty()) {
                return InterestCreationResult.builder()
                        .tripId(tripId)
                        .tripNumber(tripNumber)
                        .success(false)
                        .message(String.format("No active contract found between you (lender), transporter (%s %s), and consigner (%s %s)",
                                transporter.getFirstName(), transporter.getLastName(),
                                sender.getFirstName(), sender.getLastName()))
                        .build();
            }

            // Use the first contract (ordered by furthest expiry date)
            Contract contract = contracts.get(0);

            // Check for duplicate interest
            if (proposalRepository.existsByTripIdAndLenderIdAndContractId(tripId, lenderId, contract.getId())) {
                return InterestCreationResult.builder()
                        .tripId(tripId)
                        .tripNumber(tripNumber)
                        .success(false)
                        .message("You have already marked interest in this trip")
                        .build();
            }

            // Create the proposal
            TripFinanceProposal proposal = TripFinanceProposal.builder()
                    .trip(trip)
                    .lender(lender)
                    .contract(contract)
                    .status(ProposalStatus.PENDING)
                    .proposedAt(LocalDateTime.now())
                    .createdBy(lender)
                    .build();

            TripFinanceProposal saved = proposalRepository.save(proposal);

            log.debug("Interest created successfully for trip {} using contract {}", tripId, contract.getId());

            return InterestCreationResult.builder()
                    .tripId(tripId)
                    .tripNumber(tripNumber)
                    .success(true)
                    .message(String.format("Interest marked successfully (Contract ID: %d)", contract.getId()))
                    .interestId(saved.getId())
                    .build();

        } catch (ResourceNotFoundException e) {
            log.warn("Trip not found: {}", tripId);
            return InterestCreationResult.builder()
                    .tripId(tripId)
                    .tripNumber("N/A")
                    .success(false)
                    .message("Trip not found")
                    .build();
        } catch (BadRequestException e) {
            log.warn("Validation failed for trip {}: {}", tripId, e.getMessage());
            return InterestCreationResult.builder()
                    .tripId(tripId)
                    .tripNumber("N/A")
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error processing trip {}: {}", tripId, e.getMessage(), e);
            return InterestCreationResult.builder()
                    .tripId(tripId)
                    .tripNumber("N/A")
                    .success(false)
                    .message("Internal error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceInterestForLenderDTO> getMyInterests(Long lenderId, ProposalStatus status) {
        log.debug("Fetching interests for lender {} with status {}", lenderId, status);

        List<TripFinanceProposal> proposals;
        if (status != null) {
            proposals = proposalRepository.findByLenderIdAndStatus(lenderId, status);
        } else {
            proposals = proposalRepository.findByLenderId(lenderId);
        }

        return proposals.stream()
                .map(this::mapToLenderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceInterestForLenderDTO getMyInterestById(Long lenderId, Long interestId) {
        log.debug("Fetching interest {} for lender {}", interestId, lenderId);

        TripFinanceProposal proposal = proposalRepository.findByIdAndLenderId(interestId, lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance Interest", "id", interestId));

        return mapToLenderDTO(proposal);
    }

    @Override
    public void withdrawInterest(Long lenderId, Long interestId) {
        log.debug("Lender {} withdrawing interest {}", lenderId, interestId);

        TripFinanceProposal proposal = proposalRepository.findByIdAndLenderId(interestId, lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance Interest", "id", interestId));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Only pending interests can be withdrawn. Current status: " + proposal.getStatus());
        }

        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposal.setRespondedAt(LocalDateTime.now());
        proposal.setUpdatedBy(proposal.getLender());

        proposalRepository.save(proposal);
        log.info("Interest {} withdrawn successfully", interestId);
    }

    // ==================== Transporter Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<FinanceInterestForTransporterDTO> getInterestsForMyTrip(Long transporterId, Long tripId) {
        log.debug("Fetching interests for trip {} of transporter {}", tripId, transporterId);

        // Verify trip belongs to transporter
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (!trip.getCreatedByUser().getId().equals(transporterId)) {
            throw new BadRequestException("This trip does not belong to you");
        }

        List<TripFinanceProposal> proposals = proposalRepository.findByTripId(tripId);

        return proposals.stream()
                .map(this::mapToTransporterDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void acceptLender(Long transporterId, Long interestId) {
        log.debug("Transporter {} accepting interest {}", transporterId, interestId);

        TripFinanceProposal proposal = proposalRepository.findByIdAndTransporterId(interestId, transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance Interest", "id", interestId));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Only pending interests can be accepted. Current status: " + proposal.getStatus());
        }

        // Check if trip already has an accepted proposal
        if (proposalRepository.existsByTripIdAndStatus(proposal.getTrip().getId(), ProposalStatus.ACCEPTED)) {
            throw new BadRequestException("This trip already has an accepted financing proposal");
        }

        // Accept this proposal
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setRespondedAt(LocalDateTime.now());
        proposal.setUpdatedBy(proposal.getTrip().getCreatedByUser());
        proposalRepository.save(proposal);

        // Link trip to the accepted contract and update financial terms
        Trip trip = proposal.getTrip();
        Contract contract = proposal.getContract();
        trip.setContract(contract);
        trip.setInterestRate(contract.getInterestRate());
        trip.setMaturityDays(contract.getMaturityDays());
        tripRepository.save(trip);

        // Reject all other pending proposals for this trip
        int rejectedCount = proposalRepository.rejectOtherPendingProposals(proposal.getTrip().getId(), interestId);

        log.info("Interest {} accepted successfully. Trip linked to contract {}. {} other proposals auto-rejected",
                 interestId, contract.getId(), rejectedCount);
    }

    @Override
    public void rejectLender(Long transporterId, Long interestId) {
        log.debug("Transporter {} rejecting interest {}", transporterId, interestId);

        TripFinanceProposal proposal = proposalRepository.findByIdAndTransporterId(interestId, transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Finance Interest", "id", interestId));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Only pending interests can be rejected. Current status: " + proposal.getStatus());
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        proposal.setRespondedAt(LocalDateTime.now());
        proposal.setUpdatedBy(proposal.getTrip().getCreatedByUser());

        proposalRepository.save(proposal);
        log.info("Interest {} rejected successfully", interestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceInterestForTransporterDTO> getAllInterestsForMyTrips(Long transporterId) {
        log.debug("Fetching all interests for transporter {}", transporterId);

        List<TripFinanceProposal> proposals = proposalRepository.findAllByTransporterId(transporterId);

        return proposals.stream()
                .map(this::mapToTransporterDTO)
                .collect(Collectors.toList());
    }

    // ==================== Validation Methods ====================

    private void validateTripForFinancing(Trip trip) {
        // Add any trip-specific validation here
        // For example, check if trip is in a state that allows financing
        // This depends on your business rules
    }

    // ==================== Mapping Methods ====================

    private FinanceInterestForLenderDTO mapToLenderDTO(TripFinanceProposal proposal) {
        Trip trip = proposal.getTrip();
        Contract contract = proposal.getContract();
        User transporter = trip.getCreatedByUser();

        return FinanceInterestForLenderDTO.builder()
                .id(proposal.getId())
                .status(proposal.getStatus())
                .interestedAt(proposal.getProposedAt())
                .respondedAt(proposal.getRespondedAt())
                .tripId(trip.getId())
                .tripNumber(generateTripNumber(trip))
                .origin(trip.getPickup())
                .destination(trip.getDestination())
                .estimatedAmount(trip.getLoanAmount())
                .transporterId(transporter.getId())
                .transporterName(transporter.getFirstName() + " " + transporter.getLastName())
                .transporterCompanyName(transporter.getCompany() != null ? transporter.getCompany().getName() : "N/A")
                .contractId(contract.getId())
                .interestRate(contract.getInterestRate())
                .maturityDays(contract.getMaturityDays())
                .build();
    }

    private FinanceInterestForTransporterDTO mapToTransporterDTO(TripFinanceProposal proposal) {
        User lender = proposal.getLender();
        Contract contract = proposal.getContract();

        return FinanceInterestForTransporterDTO.builder()
                .id(proposal.getId())
                .status(proposal.getStatus())
                .interestedAt(proposal.getProposedAt())
                .respondedAt(proposal.getRespondedAt())
                .lenderId(lender.getId())
                .lenderName(lender.getFirstName() + " " + lender.getLastName())
                .lenderCompanyName(lender.getCompany() != null ? lender.getCompany().getName() : "N/A")
                .contractId(contract.getId())
                .interestRate(contract.getInterestRate())
                .maturityDays(contract.getMaturityDays())
                .contractExpiryDate(contract.getExpiryDate())
                .loanPercent(contract.getLoanPercent())
                .ltv(contract.getLtv())
                .penaltyRatio(contract.getPenaltyRatio())
                .build();
    }

    private String generateTripNumber(Trip trip) {
        // Generate a trip number if it doesn't exist
        // You might have a tripNumber field in Trip entity, or generate one
        return "TRP-" + trip.getId();
    }
}
