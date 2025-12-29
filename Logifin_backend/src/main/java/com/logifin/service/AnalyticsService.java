package com.logifin.service;

import com.logifin.dto.LenderAnalyticsDTO;
import com.logifin.dto.ShipperAnalyticsDTO;
import com.logifin.dto.TransporterAnalyticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AnalyticsService {

    /**
     * Get comprehensive analytics for a lender
     */
    LenderAnalyticsDTO getLenderAnalytics(Long lenderId, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Get lender analytics with pagination and filters
     */
    Page<LenderAnalyticsDTO.LenderInvestmentDTO> getLenderInvestments(
            Long lenderId, Long transporterId, Long tripId, String status, Pageable pageable);

    /**
     * Get comprehensive analytics for a transporter
     */
    TransporterAnalyticsDTO getTransporterAnalytics(Long transporterId, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Get transporter analytics with pagination and filters
     */
    Page<TransporterAnalyticsDTO.TransporterBorrowingDTO> getTransporterBorrowings(
            Long transporterId, Long lenderId, Long tripId, String status, Pageable pageable);

    /**
     * Get comprehensive analytics for a shipper
     */
    ShipperAnalyticsDTO getShipperAnalytics(Long shipperId, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Get shipper analytics with pagination and filters
     */
    Page<ShipperAnalyticsDTO.ShipperPaymentDTO> getShipperPayments(
            Long shipperId, Long transporterId, Long tripId, String status, Pageable pageable);
}
