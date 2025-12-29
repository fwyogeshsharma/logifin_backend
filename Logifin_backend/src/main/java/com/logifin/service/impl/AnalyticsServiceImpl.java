package com.logifin.service.impl;

import com.logifin.dto.LenderAnalyticsDTO;
import com.logifin.dto.ShipperAnalyticsDTO;
import com.logifin.dto.TransporterAnalyticsDTO;
import com.logifin.entity.*;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.*;
import com.logifin.service.AnalyticsService;
import com.logifin.util.FinancialCalculationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEntryRepository entryRepository;
    private final TripRepository tripRepository;
    private final ContractRepository contractRepository;
    private final ManualTransferRequestRepository manualTransferRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public LenderAnalyticsDTO getLenderAnalytics(Long lenderId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching lender analytics for lender ID: {}", lenderId);

        User lender = userRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lender not found with ID: " + lenderId));

        // Get wallet balance
        BigDecimal walletBalance = walletRepository.findByUserId(lenderId)
                .map(wallet -> entryRepository.getLatestBalanceSnapshot(wallet.getId())
                        .orElse(entryRepository.calculateWalletBalance(wallet.getId())))
                .orElse(BigDecimal.ZERO);

        // Get all financing transactions where lender provided money
        List<Transaction> financingTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionPurpose() != null &&
                            t.getTransactionPurpose().equals("FINANCING"))
                .filter(t -> {
                    // Check if this lender was the source (from contract wallet owned by lender)
                    return manualTransferRequestRepository.findByTransactionId(t.getTransactionId())
                            .map(mtr -> mtr.getFromUserId().equals(lenderId))
                            .orElse(false);
                })
                .filter(t -> filterByDateRange(t.getCreatedAt(), fromDate, toDate))
                .collect(Collectors.toList());

        // Get all repayment transactions where lender received money
        List<Transaction> repaymentTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionPurpose() != null &&
                            t.getTransactionPurpose().equals("REPAYMENT"))
                .filter(t -> {
                    // Check if this lender was the receiver
                    return manualTransferRequestRepository.findByTransactionId(t.getTransactionId())
                            .map(mtr -> mtr.getToUserId().equals(lenderId))
                            .orElse(false);
                })
                .filter(t -> filterByDateRange(t.getCreatedAt(), fromDate, toDate))
                .collect(Collectors.toList());

        // Build investment list with profit calculations
        List<LenderAnalyticsDTO.LenderInvestmentDTO> investments = buildLenderInvestments(
                financingTransactions, repaymentTransactions);

        // Calculate summary metrics
        BigDecimal totalAmountLent = investments.stream()
                .map(LenderAnalyticsDTO.LenderInvestmentDTO::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountReceived = investments.stream()
                .filter(inv -> "REPAID".equals(inv.getStatus()))
                .map(LenderAnalyticsDTO.LenderInvestmentDTO::getTotalRepaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = investments.stream()
                .map(LenderAnalyticsDTO.LenderInvestmentDTO::getNetProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal investedAmount = investments.stream()
                .filter(inv -> "ACTIVE".equals(inv.getStatus()))
                .map(LenderAnalyticsDTO.LenderInvestmentDTO::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRepayments = investments.stream()
                .filter(inv -> "ACTIVE".equals(inv.getStatus()) || "PENDING".equals(inv.getStatus()))
                .map(inv -> inv.getPrincipalAmount().add(inv.getInterestEarned()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTripsFinanced = investments.size();
        int activeTrips = (int) investments.stream().filter(inv -> "ACTIVE".equals(inv.getStatus())).count();
        int completedTrips = (int) investments.stream().filter(inv -> "REPAID".equals(inv.getStatus())).count();

        return LenderAnalyticsDTO.builder()
                .lenderId(lenderId)
                .lenderName(lender.getFirstName() + " " + lender.getLastName())
                .lenderEmail(lender.getEmail())
                .walletBalance(walletBalance)
                .investedAmount(investedAmount)
                .totalProfit(totalProfit)
                .totalTripsFinanced(totalTripsFinanced)
                .activeTrips(activeTrips)
                .completedTrips(completedTrips)
                .totalAmountLent(totalAmountLent)
                .totalAmountReceived(totalAmountReceived)
                .pendingRepayments(pendingRepayments)
                .investments(investments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LenderAnalyticsDTO.LenderInvestmentDTO> getLenderInvestments(
            Long lenderId, Long transporterId, Long tripId, String status, Pageable pageable) {

        LenderAnalyticsDTO analytics = getLenderAnalytics(lenderId, null, null);
        List<LenderAnalyticsDTO.LenderInvestmentDTO> filtered = analytics.getInvestments().stream()
                .filter(inv -> transporterId == null || inv.getTransporterName().contains(String.valueOf(transporterId)))
                .filter(inv -> tripId == null || inv.getTripId().equals(tripId))
                .filter(inv -> status == null || inv.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<LenderAnalyticsDTO.LenderInvestmentDTO> pageContent = start >= filtered.size() ?
                Collections.emptyList() : filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    @Override
    @Transactional(readOnly = true)
    public TransporterAnalyticsDTO getTransporterAnalytics(Long transporterId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching transporter analytics for transporter ID: {}", transporterId);

        User transporter = userRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with ID: " + transporterId));

        // Get wallet balance
        BigDecimal walletBalance = walletRepository.findByUserId(transporterId)
                .map(wallet -> entryRepository.getLatestBalanceSnapshot(wallet.getId())
                        .orElse(entryRepository.calculateWalletBalance(wallet.getId())))
                .orElse(BigDecimal.ZERO);

        // Get all transactions where transporter received financing
        List<Transaction> borrowingTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionPurpose() != null &&
                            t.getTransactionPurpose().equals("FINANCING"))
                .filter(t -> {
                    return manualTransferRequestRepository.findByTransactionId(t.getTransactionId())
                            .map(mtr -> mtr.getToUserId().equals(transporterId))
                            .orElse(false);
                })
                .filter(t -> filterByDateRange(t.getCreatedAt(), fromDate, toDate))
                .collect(Collectors.toList());

        // Get all transactions where transporter repaid
        List<Transaction> repaymentTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionPurpose() != null &&
                            t.getTransactionPurpose().equals("REPAYMENT"))
                .filter(t -> {
                    return manualTransferRequestRepository.findByTransactionId(t.getTransactionId())
                            .map(mtr -> mtr.getFromUserId().equals(transporterId))
                            .orElse(false);
                })
                .filter(t -> filterByDateRange(t.getCreatedAt(), fromDate, toDate))
                .collect(Collectors.toList());

        // Build borrowing list
        List<TransporterAnalyticsDTO.TransporterBorrowingDTO> borrowings = buildTransporterBorrowings(
                borrowingTransactions, repaymentTransactions);

        // Calculate summary metrics
        BigDecimal totalBorrowed = borrowings.stream()
                .map(TransporterAnalyticsDTO.TransporterBorrowingDTO::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRepaid = borrowings.stream()
                .filter(b -> "REPAID".equals(b.getStatus()))
                .map(TransporterAnalyticsDTO.TransporterBorrowingDTO::getTotalRepaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRepayment = borrowings.stream()
                .filter(b -> "BORROWED".equals(b.getStatus()) || "PENDING".equals(b.getStatus()))
                .map(b -> b.getPrincipalAmount().add(b.getInterestPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenueFromShippers = borrowings.stream()
                .map(b -> b.getRevenueFromShipper() != null ? b.getRevenueFromShipper() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterestPaid = borrowings.stream()
                .map(TransporterAnalyticsDTO.TransporterBorrowingDTO::getInterestPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = borrowings.stream()
                .map(TransporterAnalyticsDTO.TransporterBorrowingDTO::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTrips = borrowings.size();
        int activeTrips = (int) borrowings.stream().filter(b -> "BORROWED".equals(b.getStatus())).count();
        int completedTrips = (int) borrowings.stream().filter(b -> "REPAID".equals(b.getStatus())).count();

        return TransporterAnalyticsDTO.builder()
                .transporterId(transporterId)
                .transporterName(transporter.getFirstName() + " " + transporter.getLastName())
                .transporterEmail(transporter.getEmail())
                .walletBalance(walletBalance)
                .totalBorrowed(totalBorrowed)
                .totalRepaid(totalRepaid)
                .pendingRepayment(pendingRepayment)
                .totalTrips(totalTrips)
                .activeTrips(activeTrips)
                .completedTrips(completedTrips)
                .totalRevenueFromShippers(totalRevenueFromShippers)
                .totalInterestPaid(totalInterestPaid)
                .totalProfit(totalProfit)
                .borrowings(borrowings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransporterAnalyticsDTO.TransporterBorrowingDTO> getTransporterBorrowings(
            Long transporterId, Long lenderId, Long tripId, String status, Pageable pageable) {

        TransporterAnalyticsDTO analytics = getTransporterAnalytics(transporterId, null, null);
        List<TransporterAnalyticsDTO.TransporterBorrowingDTO> filtered = analytics.getBorrowings().stream()
                .filter(b -> lenderId == null || b.getLenderName().contains(String.valueOf(lenderId)))
                .filter(b -> tripId == null || b.getTripId().equals(tripId))
                .filter(b -> status == null || b.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<TransporterAnalyticsDTO.TransporterBorrowingDTO> pageContent = start >= filtered.size() ?
                Collections.emptyList() : filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperAnalyticsDTO getShipperAnalytics(Long shipperId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching shipper analytics for shipper ID: {}", shipperId);

        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with ID: " + shipperId));

        // Get all trips related to this shipper
        // For now, we'll use a simple approach - in production you'd have a shipper_id in trips table
        List<Trip> allTrips = tripRepository.findAll().stream()
                .filter(t -> filterByDateRange(t.getCreatedAt(), fromDate, toDate))
                .collect(Collectors.toList());

        // Build payment list
        List<ShipperAnalyticsDTO.ShipperPaymentDTO> payments = buildShipperPayments(allTrips, shipperId);

        // Calculate summary metrics
        BigDecimal totalAmountPaid = payments.stream()
                .map(ShipperAnalyticsDTO.ShipperPaymentDTO::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountPending = payments.stream()
                .map(ShipperAnalyticsDTO.ShipperPaymentDTO::getAmountPending)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTrips = payments.size();
        int completedTrips = (int) payments.stream().filter(p -> "PAID".equals(p.getStatus())).count();
        int pendingTrips = (int) payments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();

        return ShipperAnalyticsDTO.builder()
                .shipperId(shipperId)
                .shipperName(shipper.getFirstName() + " " + shipper.getLastName())
                .shipperEmail(shipper.getEmail())
                .totalAmountPaid(totalAmountPaid)
                .totalAmountPending(totalAmountPending)
                .totalTrips(totalTrips)
                .completedTrips(completedTrips)
                .pendingTrips(pendingTrips)
                .payments(payments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipperAnalyticsDTO.ShipperPaymentDTO> getShipperPayments(
            Long shipperId, Long transporterId, Long tripId, String status, Pageable pageable) {

        ShipperAnalyticsDTO analytics = getShipperAnalytics(shipperId, null, null);
        List<ShipperAnalyticsDTO.ShipperPaymentDTO> filtered = analytics.getPayments().stream()
                .filter(p -> transporterId == null || p.getTransporterName().contains(String.valueOf(transporterId)))
                .filter(p -> tripId == null || p.getTripId().equals(tripId))
                .filter(p -> status == null || p.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<ShipperAnalyticsDTO.ShipperPaymentDTO> pageContent = start >= filtered.size() ?
                Collections.emptyList() : filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    // Helper methods

    private boolean filterByDateRange(LocalDateTime date, LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate != null && date.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && date.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private List<LenderAnalyticsDTO.LenderInvestmentDTO> buildLenderInvestments(
            List<Transaction> financingTransactions, List<Transaction> repaymentTransactions) {

        Map<Long, LenderAnalyticsDTO.LenderInvestmentDTO> investmentMap = new HashMap<>();

        // Process financing transactions
        for (Transaction financing : financingTransactions) {
            if (financing.getTripId() == null) continue;

            Optional<ManualTransferRequest> mtrOpt = manualTransferRequestRepository.findByTransactionId(financing.getTransactionId());
            if (!mtrOpt.isPresent()) continue;

            ManualTransferRequest mtr = mtrOpt.get();
            Optional<Trip> tripOpt = tripRepository.findById(financing.getTripId());
            if (!tripOpt.isPresent()) continue;

            Trip trip = tripOpt.get();
            User transporter = userRepository.findById(mtr.getToUserId()).orElse(null);

            LenderAnalyticsDTO.LenderInvestmentDTO investment = LenderAnalyticsDTO.LenderInvestmentDTO.builder()
                    .tripId(trip.getId())
                    .contractId(financing.getContractId())
                    .tripDetails(trip.getPickup() + " to " + trip.getDestination())
                    .transporterName(transporter != null ? transporter.getFirstName() + " " + transporter.getLastName() : "Unknown")
                    .principalAmount(mtr.getAmount())
                    .interestRate(trip.getInterestRate())
                    .daysUsed(0)
                    .interestEarned(BigDecimal.ZERO)
                    .totalRepaid(BigDecimal.ZERO)
                    .portalFeeDeducted(BigDecimal.ZERO)
                    .netProfit(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .financedDate(financing.getCreatedAt())
                    .build();

            investmentMap.put(trip.getId(), investment);
        }

        // Process repayment transactions
        for (Transaction repayment : repaymentTransactions) {
            if (repayment.getTripId() == null) continue;

            LenderAnalyticsDTO.LenderInvestmentDTO investment = investmentMap.get(repayment.getTripId());
            if (investment == null) continue;

            Optional<ManualTransferRequest> mtrOpt = manualTransferRequestRepository.findByTransactionId(repayment.getTransactionId());
            if (!mtrOpt.isPresent()) continue;

            ManualTransferRequest mtr = mtrOpt.get();
            BigDecimal totalRepaid = mtr.getAmount();
            BigDecimal interestEarned = totalRepaid.subtract(investment.getPrincipalAmount());
            long daysUsed = FinancialCalculationUtil.calculateDaysBetween(
                    investment.getFinancedDate(), repayment.getCreatedAt());

            // Estimate portal fee (this should ideally be stored separately)
            BigDecimal portalFeeDeducted = BigDecimal.ZERO; // Can be calculated from metadata if stored

            investment.setDaysUsed((int) daysUsed);
            investment.setInterestEarned(interestEarned);
            investment.setTotalRepaid(totalRepaid);
            investment.setPortalFeeDeducted(portalFeeDeducted);
            investment.setNetProfit(interestEarned.subtract(portalFeeDeducted));
            investment.setStatus("REPAID");
            investment.setRepaymentDate(repayment.getCreatedAt());
        }

        return new ArrayList<>(investmentMap.values());
    }

    private List<TransporterAnalyticsDTO.TransporterBorrowingDTO> buildTransporterBorrowings(
            List<Transaction> borrowingTransactions, List<Transaction> repaymentTransactions) {

        Map<Long, TransporterAnalyticsDTO.TransporterBorrowingDTO> borrowingMap = new HashMap<>();

        // Process borrowing transactions
        for (Transaction borrowing : borrowingTransactions) {
            if (borrowing.getTripId() == null) continue;

            Optional<ManualTransferRequest> mtrOpt = manualTransferRequestRepository.findByTransactionId(borrowing.getTransactionId());
            if (!mtrOpt.isPresent()) continue;

            ManualTransferRequest mtr = mtrOpt.get();
            Optional<Trip> tripOpt = tripRepository.findById(borrowing.getTripId());
            if (!tripOpt.isPresent()) continue;

            Trip trip = tripOpt.get();
            User lender = userRepository.findById(mtr.getFromUserId()).orElse(null);

            TransporterAnalyticsDTO.TransporterBorrowingDTO borrowingDTO = TransporterAnalyticsDTO.TransporterBorrowingDTO.builder()
                    .tripId(trip.getId())
                    .contractId(borrowing.getContractId())
                    .tripDetails(trip.getPickup() + " to " + trip.getDestination())
                    .lenderName(lender != null ? lender.getFirstName() + " " + lender.getLastName() : "Unknown")
                    .principalAmount(mtr.getAmount())
                    .interestRate(trip.getInterestRate())
                    .daysUsed(0)
                    .interestPaid(BigDecimal.ZERO)
                    .totalRepaid(BigDecimal.ZERO)
                    .revenueFromShipper(BigDecimal.ZERO)
                    .profit(BigDecimal.ZERO)
                    .status("BORROWED")
                    .borrowedDate(borrowing.getCreatedAt())
                    .build();

            borrowingMap.put(trip.getId(), borrowingDTO);
        }

        // Process repayment transactions
        for (Transaction repayment : repaymentTransactions) {
            if (repayment.getTripId() == null) continue;

            TransporterAnalyticsDTO.TransporterBorrowingDTO borrowingDTO = borrowingMap.get(repayment.getTripId());
            if (borrowingDTO == null) continue;

            Optional<ManualTransferRequest> mtrOpt = manualTransferRequestRepository.findByTransactionId(repayment.getTransactionId());
            if (!mtrOpt.isPresent()) continue;

            ManualTransferRequest mtr = mtrOpt.get();
            BigDecimal totalRepaid = mtr.getAmount();
            BigDecimal interestPaid = totalRepaid.subtract(borrowingDTO.getPrincipalAmount());
            long daysUsed = FinancialCalculationUtil.calculateDaysBetween(
                    borrowingDTO.getBorrowedDate(), repayment.getCreatedAt());

            // Revenue from shipper would need to be tracked separately
            // For now, assume revenue = totalRepaid + some margin
            BigDecimal revenueFromShipper = BigDecimal.ZERO; // Should be fetched from actual shipper payment records

            borrowingDTO.setDaysUsed((int) daysUsed);
            borrowingDTO.setInterestPaid(interestPaid);
            borrowingDTO.setTotalRepaid(totalRepaid);
            borrowingDTO.setRevenueFromShipper(revenueFromShipper);
            borrowingDTO.setProfit(revenueFromShipper.subtract(totalRepaid));
            borrowingDTO.setStatus("REPAID");
            borrowingDTO.setRepaymentDate(repayment.getCreatedAt());
        }

        return new ArrayList<>(borrowingMap.values());
    }

    private List<ShipperAnalyticsDTO.ShipperPaymentDTO> buildShipperPayments(List<Trip> trips, Long shipperId) {
        List<ShipperAnalyticsDTO.ShipperPaymentDTO> payments = new ArrayList<>();

        for (Trip trip : trips) {
            // In a real implementation, you would check if this shipper is associated with this trip
            // For now, we'll create payment records for all trips

            ShipperAnalyticsDTO.ShipperPaymentDTO payment = ShipperAnalyticsDTO.ShipperPaymentDTO.builder()
                    .tripId(trip.getId())
                    .contractId(trip.getContract() != null ? trip.getContract().getId() : null)
                    .tripDetails(trip.getPickup() + " to " + trip.getDestination())
                    .transporterName(trip.getTransporter() != null ?
                            trip.getTransporter().getFirstName() + " " + trip.getTransporter().getLastName() : null)
                    .invoiceNumber("INV-" + trip.getId())
                    .tripCost(trip.getLoanAmount())
                    .amountPaid(trip.getStatus() == Trip.TripStatus.COMPLETED ? trip.getLoanAmount() : BigDecimal.ZERO)
                    .amountPending(trip.getStatus() != Trip.TripStatus.COMPLETED ? trip.getLoanAmount() : BigDecimal.ZERO)
                    .status(trip.getStatus() == Trip.TripStatus.COMPLETED ? "PAID" : "PENDING")
                    .tripCompletedDate(trip.getStatus() == Trip.TripStatus.COMPLETED ? trip.getUpdatedAt() : null)
                    .paymentDate(trip.getStatus() == Trip.TripStatus.COMPLETED ? trip.getUpdatedAt() : null)
                    .build();

            payments.add(payment);
        }

        return payments;
    }
}
