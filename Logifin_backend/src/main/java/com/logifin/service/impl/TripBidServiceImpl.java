package com.logifin.service.impl;

import com.logifin.dto.*;
import com.logifin.entity.Company;
import com.logifin.entity.Trip;
import com.logifin.entity.TripBid;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.TripBidRepository;
import com.logifin.repository.TripRepository;
import com.logifin.repository.UserRepository;
import com.logifin.repository.specification.TripBidSpecification;
import com.logifin.service.TripBidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TripBidService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TripBidServiceImpl implements TripBidService {

    private final TripBidRepository tripBidRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    private static final int DEFAULT_BID_EXPIRY_DAYS = 7;

    // ==================== CRUD Operations ====================

    @Override
    public TripBidResponseDTO createBid(TripBidRequestDTO requestDTO, Long lenderId) {
        log.info("Creating bid for trip {} by lender {}", requestDTO.getTripId(), lenderId);

        // Validate trip exists
        Trip trip = tripRepository.findById(requestDTO.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", requestDTO.getTripId()));

        // Validate trip is active
        if (trip.getStatus() != Trip.TripStatus.ACTIVE) {
            throw new IllegalStateException("Cannot bid on trip with status: " + trip.getStatus());
        }

        // Validate lender exists
        User lender = userRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", lenderId));

        // Validate lender has company
        if (lender.getCompany() == null) {
            throw new IllegalStateException("Lender must be associated with a company to place bids");
        }

        // Check if lender already has active bid on this trip
        List<TripBid.BidStatus> activeStatuses = Arrays.asList(TripBid.BidStatus.PENDING, TripBid.BidStatus.COUNTERED);
        if (tripBidRepository.existsByTripIdAndLenderIdAndStatusIn(requestDTO.getTripId(), lenderId, activeStatuses)) {
            throw new DuplicateResourceException("You already have an active bid on this trip");
        }

        // Create bid
        TripBid bid = TripBid.builder()
                .trip(trip)
                .lender(lender)
                .company(lender.getCompany())
                .bidAmount(requestDTO.getBidAmount())
                .currency(requestDTO.getCurrency() != null ? requestDTO.getCurrency() : "INR")
                .interestRate(requestDTO.getInterestRate())
                .maturityDays(requestDTO.getMaturityDays())
                .notes(requestDTO.getNotes())
                .status(TripBid.BidStatus.PENDING)
                .expiresAt(requestDTO.getExpiresAt() != null
                        ? requestDTO.getExpiresAt()
                        : LocalDateTime.now().plusDays(DEFAULT_BID_EXPIRY_DAYS))
                .build();

        TripBid savedBid = tripBidRepository.save(bid);
        log.info("Bid created successfully with ID: {}", savedBid.getId());

        return mapToResponseDTO(savedBid);
    }

    @Override
    @Transactional(readOnly = true)
    public TripBidResponseDTO getBidById(Long bidId) {
        TripBid bid = findBidById(bidId);
        return mapToResponseDTO(bid);
    }

    @Override
    public TripBidResponseDTO updateBid(Long bidId, TripBidRequestDTO requestDTO, Long lenderId) {
        log.info("Updating bid {} by lender {}", bidId, lenderId);

        TripBid bid = findBidById(bidId);

        // Validate ownership
        if (!bid.getLender().getId().equals(lenderId)) {
            throw new IllegalStateException("You can only update your own bids");
        }

        // Validate status
        if (bid.getStatus() != TripBid.BidStatus.PENDING) {
            throw new IllegalStateException("Can only update bids with PENDING status");
        }

        // Update fields
        bid.setBidAmount(requestDTO.getBidAmount());
        if (requestDTO.getCurrency() != null) {
            bid.setCurrency(requestDTO.getCurrency());
        }
        bid.setInterestRate(requestDTO.getInterestRate());
        bid.setMaturityDays(requestDTO.getMaturityDays());
        bid.setNotes(requestDTO.getNotes());
        if (requestDTO.getExpiresAt() != null) {
            bid.setExpiresAt(requestDTO.getExpiresAt());
        }

        TripBid updatedBid = tripBidRepository.save(bid);
        log.info("Bid {} updated successfully", bidId);

        return mapToResponseDTO(updatedBid);
    }

    @Override
    public void cancelBid(Long bidId, Long lenderId) {
        log.info("Cancelling bid {} by lender {}", bidId, lenderId);

        TripBid bid = findBidById(bidId);

        // Validate ownership
        if (!bid.getLender().getId().equals(lenderId)) {
            throw new IllegalStateException("You can only cancel your own bids");
        }

        // Validate status
        if (!bid.isActive()) {
            throw new IllegalStateException("Can only cancel active bids (PENDING or COUNTERED)");
        }

        bid.setStatus(TripBid.BidStatus.CANCELLED);
        tripBidRepository.save(bid);
        log.info("Bid {} cancelled successfully", bidId);
    }

    // ==================== Bid Actions (Transporter) ====================

    @Override
    public TripBidResponseDTO acceptBid(Long bidId, Long transporterId) {
        log.info("Accepting bid {} by transporter {}", bidId, transporterId);

        TripBid bid = findBidById(bidId);

        // Validate transporter owns the trip
        validateTripOwnership(bid.getTrip(), transporterId);

        // Validate bid is active
        if (!bid.isActive()) {
            throw new IllegalStateException("Can only accept active bids");
        }

        // Check if bid has expired
        if (bid.isExpired()) {
            bid.setStatus(TripBid.BidStatus.EXPIRED);
            tripBidRepository.save(bid);
            throw new IllegalStateException("This bid has expired");
        }

        // Accept the bid
        bid.setStatus(TripBid.BidStatus.ACCEPTED);
        bid.setRespondedAt(LocalDateTime.now());
        bid.setRespondedBy(userRepository.findById(transporterId).orElse(null));

        // Reject all other pending bids for this trip
        rejectOtherBids(bid.getTrip().getId(), bidId, transporterId);

        TripBid acceptedBid = tripBidRepository.save(bid);
        log.info("Bid {} accepted successfully", bidId);

        return mapToResponseDTO(acceptedBid);
    }

    @Override
    public TripBidResponseDTO rejectBid(Long bidId, TripBidActionDTO actionDTO, Long transporterId) {
        log.info("Rejecting bid {} by transporter {}", bidId, transporterId);

        TripBid bid = findBidById(bidId);

        // Validate transporter owns the trip
        validateTripOwnership(bid.getTrip(), transporterId);

        // Validate bid is active
        if (!bid.isActive()) {
            throw new IllegalStateException("Can only reject active bids");
        }

        bid.setStatus(TripBid.BidStatus.REJECTED);
        bid.setRespondedAt(LocalDateTime.now());
        bid.setRespondedBy(userRepository.findById(transporterId).orElse(null));
        if (actionDTO != null && actionDTO.getRejectionReason() != null) {
            bid.setRejectionReason(actionDTO.getRejectionReason());
        }

        TripBid rejectedBid = tripBidRepository.save(bid);
        log.info("Bid {} rejected successfully", bidId);

        return mapToResponseDTO(rejectedBid);
    }

    @Override
    public TripBidResponseDTO counterBid(Long bidId, TripBidCounterOfferDTO counterOfferDTO, Long transporterId) {
        log.info("Making counter offer on bid {} by transporter {}", bidId, transporterId);

        TripBid bid = findBidById(bidId);

        // Validate transporter owns the trip
        validateTripOwnership(bid.getTrip(), transporterId);

        // Validate bid is pending (not already countered)
        if (bid.getStatus() != TripBid.BidStatus.PENDING) {
            throw new IllegalStateException("Can only counter bids with PENDING status");
        }

        // Check if bid has expired
        if (bid.isExpired()) {
            bid.setStatus(TripBid.BidStatus.EXPIRED);
            tripBidRepository.save(bid);
            throw new IllegalStateException("This bid has expired");
        }

        User transporter = userRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", transporterId));

        bid.setStatus(TripBid.BidStatus.COUNTERED);
        bid.setCounterAmount(counterOfferDTO.getCounterAmount());
        bid.setCounterInterestRate(counterOfferDTO.getCounterInterestRate());
        bid.setCounterMaturityDays(counterOfferDTO.getCounterMaturityDays());
        bid.setCounterNotes(counterOfferDTO.getCounterNotes());
        bid.setCounteredAt(LocalDateTime.now());
        bid.setCounteredBy(transporter);
        // Extend expiry for counter offer
        bid.setExpiresAt(LocalDateTime.now().plusDays(DEFAULT_BID_EXPIRY_DAYS));

        TripBid counteredBid = tripBidRepository.save(bid);
        log.info("Counter offer made on bid {} successfully", bidId);

        return mapToResponseDTO(counteredBid);
    }

    // ==================== Bid Actions (Lender) ====================

    @Override
    public TripBidResponseDTO acceptCounterOffer(Long bidId, Long lenderId) {
        log.info("Accepting counter offer on bid {} by lender {}", bidId, lenderId);

        TripBid bid = findBidById(bidId);

        // Validate ownership
        if (!bid.getLender().getId().equals(lenderId)) {
            throw new IllegalStateException("You can only accept counter offers on your own bids");
        }

        // Validate status
        if (bid.getStatus() != TripBid.BidStatus.COUNTERED) {
            throw new IllegalStateException("Can only accept counter offers on COUNTERED bids");
        }

        // Check if bid has expired
        if (bid.isExpired()) {
            bid.setStatus(TripBid.BidStatus.EXPIRED);
            tripBidRepository.save(bid);
            throw new IllegalStateException("This counter offer has expired");
        }

        // Update bid with counter offer values
        bid.setBidAmount(bid.getCounterAmount());
        if (bid.getCounterInterestRate() != null) {
            bid.setInterestRate(bid.getCounterInterestRate());
        }
        if (bid.getCounterMaturityDays() != null) {
            bid.setMaturityDays(bid.getCounterMaturityDays());
        }

        bid.setStatus(TripBid.BidStatus.ACCEPTED);
        bid.setRespondedAt(LocalDateTime.now());
        bid.setRespondedBy(userRepository.findById(lenderId).orElse(null));

        // Reject all other pending bids for this trip
        rejectOtherBids(bid.getTrip().getId(), bidId, lenderId);

        TripBid acceptedBid = tripBidRepository.save(bid);
        log.info("Counter offer on bid {} accepted successfully", bidId);

        return mapToResponseDTO(acceptedBid);
    }

    @Override
    public TripBidResponseDTO rejectCounterOffer(Long bidId, Long lenderId) {
        log.info("Rejecting counter offer on bid {} by lender {}", bidId, lenderId);

        TripBid bid = findBidById(bidId);

        // Validate ownership
        if (!bid.getLender().getId().equals(lenderId)) {
            throw new IllegalStateException("You can only reject counter offers on your own bids");
        }

        // Validate status
        if (bid.getStatus() != TripBid.BidStatus.COUNTERED) {
            throw new IllegalStateException("Can only reject counter offers on COUNTERED bids");
        }

        bid.setStatus(TripBid.BidStatus.CANCELLED);
        bid.setRespondedAt(LocalDateTime.now());

        TripBid rejectedBid = tripBidRepository.save(bid);
        log.info("Counter offer on bid {} rejected successfully", bidId);

        return mapToResponseDTO(rejectedBid);
    }

    // ==================== Query Operations ====================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripBidResponseDTO> getBidsForTrip(Long tripId, Pageable pageable) {
        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip", "id", tripId);
        }

        Page<TripBid> bids = tripBidRepository.findByTripId(tripId, pageable);
        List<TripBidResponseDTO> content = bids.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(bids, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripBidResponseDTO> getActiveBidsForTrip(Long tripId) {
        return tripBidRepository.findActiveBidsByTripId(tripId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripBidResponseDTO getAcceptedBidForTrip(Long tripId) {
        return tripBidRepository.findFirstByTripIdAndStatus(tripId, TripBid.BidStatus.ACCEPTED)
                .map(this::mapToResponseDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripBidResponseDTO> getBidsByLender(Long lenderId, Pageable pageable) {
        Page<TripBid> bids = tripBidRepository.findByLenderId(lenderId, pageable);
        List<TripBidResponseDTO> content = bids.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(bids, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripBidResponseDTO> getBidsByLenderAndStatus(Long lenderId, String status, Pageable pageable) {
        TripBid.BidStatus bidStatus = TripBid.BidStatus.valueOf(status.toUpperCase());
        Page<TripBid> bids = tripBidRepository.findByLenderIdAndStatus(lenderId, bidStatus, pageable);
        List<TripBidResponseDTO> content = bids.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(bids, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripBidResponseDTO> getBidsByCompany(Long companyId, Pageable pageable) {
        Page<TripBid> bids = tripBidRepository.findByCompanyId(companyId, pageable);
        List<TripBidResponseDTO> content = bids.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(bids, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripBidResponseDTO> searchBids(TripBidSearchCriteria criteria, Pageable pageable) {
        Page<TripBid> bids = tripBidRepository.findAll(
                TripBidSpecification.buildSpecification(criteria), pageable);
        List<TripBidResponseDTO> content = bids.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(bids, content);
    }

    // ==================== Statistics ====================

    @Override
    @Transactional(readOnly = true)
    public TripBidStatisticsDTO getBidStatistics() {
        long totalBids = tripBidRepository.count();
        long pendingBids = tripBidRepository.countByStatus(TripBid.BidStatus.PENDING);
        long acceptedBids = tripBidRepository.countByStatus(TripBid.BidStatus.ACCEPTED);
        long rejectedBids = tripBidRepository.countByStatus(TripBid.BidStatus.REJECTED);
        long cancelledBids = tripBidRepository.countByStatus(TripBid.BidStatus.CANCELLED);
        long expiredBids = tripBidRepository.countByStatus(TripBid.BidStatus.EXPIRED);
        long counteredBids = tripBidRepository.countByStatus(TripBid.BidStatus.COUNTERED);

        BigDecimal totalBidAmount = tripBidRepository.getTotalBidAmount();
        BigDecimal totalAcceptedAmount = tripBidRepository.getTotalAcceptedBidAmount();
        BigDecimal averageBidAmount = tripBidRepository.getAverageBidAmount();
        BigDecimal averageInterestRate = tripBidRepository.getAverageInterestRate();
        Double averageMaturityDays = tripBidRepository.getAverageMaturityDays();

        Long uniqueLenders = tripBidRepository.countUniqueLenders();
        Long tripsWithBids = tripBidRepository.countTripsWithBids();

        BigDecimal acceptanceRate = BigDecimal.ZERO;
        if (totalBids > 0) {
            acceptanceRate = BigDecimal.valueOf(acceptedBids)
                    .divide(BigDecimal.valueOf(totalBids), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return TripBidStatisticsDTO.builder()
                .totalBids(totalBids)
                .pendingBids(pendingBids)
                .acceptedBids(acceptedBids)
                .rejectedBids(rejectedBids)
                .cancelledBids(cancelledBids)
                .expiredBids(expiredBids)
                .counteredBids(counteredBids)
                .totalBidAmount(totalBidAmount)
                .totalAcceptedAmount(totalAcceptedAmount)
                .averageBidAmount(averageBidAmount)
                .averageInterestRate(averageInterestRate)
                .averageMaturityDays(averageMaturityDays)
                .acceptanceRate(acceptanceRate)
                .uniqueLenders(uniqueLenders)
                .tripsWithBids(tripsWithBids)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripBidStatisticsDTO getBidStatisticsForTrip(Long tripId) {
        long totalBids = tripBidRepository.countByTripId(tripId);
        long pendingBids = tripBidRepository.countByTripIdAndStatus(tripId, TripBid.BidStatus.PENDING);
        long acceptedBids = tripBidRepository.countByTripIdAndStatus(tripId, TripBid.BidStatus.ACCEPTED);
        long rejectedBids = tripBidRepository.countByTripIdAndStatus(tripId, TripBid.BidStatus.REJECTED);
        long cancelledBids = tripBidRepository.countByTripIdAndStatus(tripId, TripBid.BidStatus.CANCELLED);
        long expiredBids = tripBidRepository.countByTripIdAndStatus(tripId, TripBid.BidStatus.EXPIRED);
        long counteredBids = tripBidRepository.countByTripIdAndStatus(tripId, TripBid.BidStatus.COUNTERED);

        return TripBidStatisticsDTO.builder()
                .totalBids(totalBids)
                .pendingBids(pendingBids)
                .acceptedBids(acceptedBids)
                .rejectedBids(rejectedBids)
                .cancelledBids(cancelledBids)
                .expiredBids(expiredBids)
                .counteredBids(counteredBids)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripBidStatisticsDTO getBidStatisticsForLender(Long lenderId) {
        long totalBids = tripBidRepository.countByLenderId(lenderId);

        // Get status counts using specification
        TripBidSearchCriteria pendingCriteria = TripBidSearchCriteria.builder()
                .lenderId(lenderId).status(TripBid.BidStatus.PENDING).build();
        long pendingBids = tripBidRepository.count(TripBidSpecification.buildSpecification(pendingCriteria));

        TripBidSearchCriteria acceptedCriteria = TripBidSearchCriteria.builder()
                .lenderId(lenderId).status(TripBid.BidStatus.ACCEPTED).build();
        long acceptedBids = tripBidRepository.count(TripBidSpecification.buildSpecification(acceptedCriteria));

        TripBidSearchCriteria rejectedCriteria = TripBidSearchCriteria.builder()
                .lenderId(lenderId).status(TripBid.BidStatus.REJECTED).build();
        long rejectedBids = tripBidRepository.count(TripBidSpecification.buildSpecification(rejectedCriteria));

        BigDecimal acceptanceRate = BigDecimal.ZERO;
        if (totalBids > 0) {
            acceptanceRate = BigDecimal.valueOf(acceptedBids)
                    .divide(BigDecimal.valueOf(totalBids), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return TripBidStatisticsDTO.builder()
                .totalBids(totalBids)
                .pendingBids(pendingBids)
                .acceptedBids(acceptedBids)
                .rejectedBids(rejectedBids)
                .acceptanceRate(acceptanceRate)
                .build();
    }

    // ==================== Expiry Management ====================

    @Override
    public int processExpiredBids() {
        log.info("Processing expired bids");
        int count = tripBidRepository.updateExpiredBids(LocalDateTime.now());
        log.info("Marked {} bids as expired", count);
        return count;
    }

    // ==================== Validation ====================

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveBidOnTrip(Long tripId, Long lenderId) {
        List<TripBid.BidStatus> activeStatuses = Arrays.asList(TripBid.BidStatus.PENDING, TripBid.BidStatus.COUNTERED);
        return tripBidRepository.existsByTripIdAndLenderIdAndStatusIn(tripId, lenderId, activeStatuses);
    }

    // ==================== Helper Methods ====================

    private TripBid findBidById(Long bidId) {
        return tripBidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("TripBid", "id", bidId));
    }

    private void validateTripOwnership(Trip trip, Long userId) {
        if (trip.getCreatedByUser() == null || !trip.getCreatedByUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only perform this action on your own trips");
        }
    }

    private void rejectOtherBids(Long tripId, Long acceptedBidId, Long userId) {
        List<TripBid> otherBids = tripBidRepository.findByTripIdAndStatus(tripId, TripBid.BidStatus.PENDING)
                .stream()
                .filter(b -> !b.getId().equals(acceptedBidId))
                .collect(Collectors.toList());

        // Also get countered bids
        otherBids.addAll(tripBidRepository.findByTripIdAndStatus(tripId, TripBid.BidStatus.COUNTERED)
                .stream()
                .filter(b -> !b.getId().equals(acceptedBidId))
                .collect(Collectors.toList()));

        User responder = userRepository.findById(userId).orElse(null);

        for (TripBid bid : otherBids) {
            bid.setStatus(TripBid.BidStatus.REJECTED);
            bid.setRespondedAt(LocalDateTime.now());
            bid.setRespondedBy(responder);
            bid.setRejectionReason("Another bid was accepted for this trip");
        }

        if (!otherBids.isEmpty()) {
            tripBidRepository.saveAll(otherBids);
            log.info("Rejected {} other bids for trip {}", otherBids.size(), tripId);
        }
    }

    private TripBidResponseDTO mapToResponseDTO(TripBid bid) {
        TripBidResponseDTO dto = TripBidResponseDTO.builder()
                .id(bid.getId())
                // Trip info
                .tripId(bid.getTrip().getId())
                .tripPickup(bid.getTrip().getPickup())
                .tripDestination(bid.getTrip().getDestination())
                .tripLoanAmount(bid.getTrip().getLoanAmount())
                // Lender info
                .lenderId(bid.getLender().getId())
                .lenderName(bid.getLender().getFirstName() + " " + bid.getLender().getLastName())
                .lenderEmail(bid.getLender().getEmail())
                // Company info
                .companyId(bid.getCompany().getId())
                .companyName(bid.getCompany().getName())
                // Bid details
                .bidAmount(bid.getBidAmount())
                .currency(bid.getCurrency())
                .interestRate(bid.getInterestRate())
                .maturityDays(bid.getMaturityDays())
                .status(bid.getStatus())
                .notes(bid.getNotes())
                // Counter offer
                .counterAmount(bid.getCounterAmount())
                .counterInterestRate(bid.getCounterInterestRate())
                .counterMaturityDays(bid.getCounterMaturityDays())
                .counterNotes(bid.getCounterNotes())
                .counteredAt(bid.getCounteredAt())
                // Response tracking
                .respondedAt(bid.getRespondedAt())
                .rejectionReason(bid.getRejectionReason())
                // Expiry
                .expiresAt(bid.getExpiresAt())
                .isExpired(bid.isExpired())
                // Audit
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();

        // Set countered by info
        if (bid.getCounteredBy() != null) {
            dto.setCounteredById(bid.getCounteredBy().getId());
            dto.setCounteredByName(bid.getCounteredBy().getFirstName() + " " + bid.getCounteredBy().getLastName());
        }

        // Set responded by info
        if (bid.getRespondedBy() != null) {
            dto.setRespondedById(bid.getRespondedBy().getId());
            dto.setRespondedByName(bid.getRespondedBy().getFirstName() + " " + bid.getRespondedBy().getLastName());
        }

        // Calculate totals if interest rate and maturity days are present
        if (bid.getBidAmount() != null && bid.getInterestRate() != null && bid.getMaturityDays() != null) {
            BigDecimal dailyRate = bid.getInterestRate().divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
            BigDecimal totalInterest = bid.getBidAmount()
                    .multiply(dailyRate)
                    .multiply(BigDecimal.valueOf(bid.getMaturityDays()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            dto.setTotalInterestAmount(totalInterest);
            dto.setTotalAmount(bid.getBidAmount().add(totalInterest));
        }

        return dto;
    }
}
