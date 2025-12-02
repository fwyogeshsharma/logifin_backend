package com.logifin.service.impl;

import com.logifin.dto.*;
import com.logifin.entity.Company;
import com.logifin.entity.DocumentType;
import com.logifin.entity.Trip;
import com.logifin.entity.TripDocument;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.DocumentTypeRepository;
import com.logifin.repository.TripDocumentRepository;
import com.logifin.repository.TripRepository;
import com.logifin.repository.UserRepository;
import com.logifin.repository.specification.TripSpecification;
import com.logifin.service.TripService;
import com.logifin.util.TripExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TripService.
 * Handles all trip management operations including CRUD, bulk upload, export, and statistics.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripDocumentRepository tripDocumentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final UserRepository userRepository;
    private final TripExcelParser tripExcelParser;

    private static final String[] CSV_HEADERS = {
            "ewayBillNumber", "pickup", "destination", "sender", "receiver",
            "transporter", "loanAmount", "interestRate", "maturityDays",
            "distanceKm", "loadType", "weightKg", "notes"
    };

    // ==================== CRUD Operations ====================

    @Override
    public TripResponseDTO createTrip(TripRequestDTO requestDTO, Long userId) {
        log.debug("Creating trip with E-way Bill Number: {}", requestDTO.getEwayBillNumber());

        // Check for duplicate E-way Bill Number
        if (tripRepository.existsByEwayBillNumber(requestDTO.getEwayBillNumber())) {
            throw new DuplicateResourceException("Trip", "ewayBillNumber", requestDTO.getEwayBillNumber());
        }

        // Get the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Build trip entity
        Trip trip = buildTripFromRequest(requestDTO, user);
        Trip savedTrip = tripRepository.save(trip);

        // Handle E-way Bill image if provided
        if (StringUtils.hasText(requestDTO.getEwayBillImageBase64())) {
            uploadDocumentFromBase64(savedTrip, DocumentType.CODE_EWAY_BILL,
                    requestDTO.getEwayBillImageBase64(), "E-Way Bill Image", user);
        }

        log.info("Trip created successfully with ID: {}", savedTrip.getId());
        return mapToResponseDTO(savedTrip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO getTripById(Long tripId) {
        log.debug("Fetching trip by ID: {}", tripId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));
        return mapToResponseDTO(trip);
    }

    @Override
    public TripResponseDTO updateTrip(Long tripId, TripRequestDTO requestDTO, Long userId) {
        log.debug("Updating trip ID: {}", tripId);

        Trip existingTrip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        // Check for duplicate E-way Bill Number (if changed)
        if (!existingTrip.getEwayBillNumber().equals(requestDTO.getEwayBillNumber())
                && tripRepository.existsByEwayBillNumber(requestDTO.getEwayBillNumber())) {
            throw new DuplicateResourceException("Trip", "ewayBillNumber", requestDTO.getEwayBillNumber());
        }

        // Update fields
        updateTripFromRequest(existingTrip, requestDTO);
        Trip updatedTrip = tripRepository.save(existingTrip);

        // Handle E-way Bill image if provided
        if (StringUtils.hasText(requestDTO.getEwayBillImageBase64())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            // Delete existing and upload new
            tripDocumentRepository.deleteByTripIdAndDocumentTypeCode(tripId, DocumentType.CODE_EWAY_BILL);
            uploadDocumentFromBase64(updatedTrip, DocumentType.CODE_EWAY_BILL,
                    requestDTO.getEwayBillImageBase64(), "E-Way Bill Image", user);
        }

        log.info("Trip updated successfully: {}", tripId);
        return mapToResponseDTO(updatedTrip);
    }

    @Override
    public void deleteTrip(Long tripId) {
        log.debug("Deleting trip ID: {}", tripId);
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip", "id", tripId);
        }
        // Documents will be cascade deleted
        tripRepository.deleteById(tripId);
        log.info("Trip deleted successfully: {}", tripId);
    }

    // ==================== Search and Pagination ====================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripResponseDTO> getAllTrips(Pageable pageable) {
        log.debug("Fetching all trips with pagination");
        Page<Trip> tripPage = tripRepository.findAll(pageable);
        return createPagedResponse(tripPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripResponseDTO> searchTrips(TripSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching trips with criteria");
        Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
        Page<Trip> tripPage = tripRepository.findAll(spec, pageable);
        return createPagedResponse(tripPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripResponseDTO> searchByKeyword(String keyword, Pageable pageable) {
        log.debug("Searching trips by keyword: {}", keyword);
        Page<Trip> tripPage = tripRepository.searchByKeyword(keyword, pageable);
        return createPagedResponse(tripPage);
    }

    // ==================== Bulk Operations ====================

    @Override
    public BulkUploadResponseDTO bulkUploadTrips(MultipartFile file, Long userId) {
        log.debug("Processing bulk upload for user: {}", userId);
        long startTime = System.currentTimeMillis();

        BulkUploadResponseDTO response = BulkUploadResponseDTO.builder()
                .successfulTripIds(new ArrayList<>())
                .errors(new ArrayList<>())
                .build();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String filename = file.getOriginalFilename();
        if (filename == null) {
            response.addError(ErrorRowDTO.parsingError(0, null, "Invalid file: no filename"));
            response.calculateSummary();
            return response;
        }

        try {
            List<TripRequestDTO> tripRequests;
            String lowerFilename = filename.toLowerCase();

            if (lowerFilename.endsWith(".csv")) {
                tripRequests = tripExcelParser.parseCsv(file, response);
            } else if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
                tripRequests = tripExcelParser.parseExcel(file, response);
            } else {
                response.addError(ErrorRowDTO.parsingError(0, null,
                        "Unsupported file format. Please use CSV, XLS, or XLSX"));
                response.calculateSummary();
                return response;
            }

            // Get existing E-way Bill Numbers for duplicate checking
            List<String> ewayBillNumbers = tripRequests.stream()
                    .map(TripRequestDTO::getEwayBillNumber)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            Set<String> existingEwayBillNumbers = new HashSet<>(
                    tripRepository.findExistingEwayBillNumbers(ewayBillNumbers));

            // Process each trip
            int rowNumber = 2; // Start from 2 (row 1 is header)
            for (TripRequestDTO request : tripRequests) {
                try {
                    if (existingEwayBillNumbers.contains(request.getEwayBillNumber())) {
                        response.addError(ErrorRowDTO.duplicateError(rowNumber, request.getEwayBillNumber()));
                    } else {
                        Trip trip = buildTripFromRequest(request, user);
                        Trip savedTrip = tripRepository.save(trip);
                        response.addSuccessfulTripId(savedTrip.getId());
                        existingEwayBillNumbers.add(request.getEwayBillNumber()); // Prevent duplicates within file
                    }
                } catch (Exception e) {
                    log.error("Error saving trip at row {}: {}", rowNumber, e.getMessage());
                    response.addError(ErrorRowDTO.builder()
                            .rowNumber(rowNumber)
                            .ewayBillNumber(request.getEwayBillNumber())
                            .errorType(ErrorRowDTO.ErrorType.DATABASE_ERROR)
                            .errors(new ArrayList<>(Collections.singletonList(e.getMessage())))
                            .build());
                }
                rowNumber++;
            }

        } catch (Exception e) {
            log.error("Error processing bulk upload: {}", e.getMessage());
            response.addError(ErrorRowDTO.parsingError(0, null, "Error processing file: " + e.getMessage()));
        }

        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        response.calculateSummary();
        log.info("Bulk upload completed: {} successes, {} failures", response.getSuccessCount(), response.getFailureCount());
        return response;
    }

    // ==================== Export Operations ====================

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTripsToCsv(TripSearchCriteria criteria) {
        log.debug("Exporting trips to CSV");
        List<Trip> trips = getTripsForExport(criteria);

        StringBuilder csv = new StringBuilder();
        // Header
        csv.append(String.join(",", CSV_HEADERS)).append(",status,createdAt\n");

        // Data rows
        for (Trip trip : trips) {
            csv.append(escapeCsvField(trip.getEwayBillNumber())).append(",");
            csv.append(escapeCsvField(trip.getPickup())).append(",");
            csv.append(escapeCsvField(trip.getDestination())).append(",");
            csv.append(escapeCsvField(trip.getSender())).append(",");
            csv.append(escapeCsvField(trip.getReceiver())).append(",");
            csv.append(escapeCsvField(trip.getTransporter())).append(",");
            csv.append(trip.getLoanAmount()).append(",");
            csv.append(trip.getInterestRate()).append(",");
            csv.append(trip.getMaturityDays()).append(",");
            csv.append(trip.getDistanceKm() != null ? trip.getDistanceKm() : "").append(",");
            csv.append(escapeCsvField(trip.getLoadType())).append(",");
            csv.append(trip.getWeightKg() != null ? trip.getWeightKg() : "").append(",");
            csv.append(escapeCsvField(trip.getNotes())).append(",");
            csv.append(trip.getStatus()).append(",");
            csv.append(trip.getCreatedAt() != null ? trip.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
            csv.append("\n");
        }

        return csv.toString().getBytes();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTripsToExcel(TripSearchCriteria criteria) {
        log.debug("Exporting trips to Excel");
        List<Trip> trips = getTripsForExport(criteria);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Trips");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"E-way Bill Number", "Pickup", "Destination", "Sender", "Receiver",
                    "Transporter", "Loan Amount", "Interest Rate", "Maturity Days",
                    "Distance (km)", "Load Type", "Weight (kg)", "Notes", "Status", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Trip trip : trips) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(trip.getEwayBillNumber());
                row.createCell(1).setCellValue(trip.getPickup());
                row.createCell(2).setCellValue(trip.getDestination());
                row.createCell(3).setCellValue(trip.getSender());
                row.createCell(4).setCellValue(trip.getReceiver());
                row.createCell(5).setCellValue(trip.getTransporter());
                row.createCell(6).setCellValue(trip.getLoanAmount().doubleValue());
                row.createCell(7).setCellValue(trip.getInterestRate().doubleValue());
                row.createCell(8).setCellValue(trip.getMaturityDays());
                row.createCell(9).setCellValue(trip.getDistanceKm() != null ? trip.getDistanceKm().doubleValue() : 0);
                row.createCell(10).setCellValue(trip.getLoadType() != null ? trip.getLoadType() : "");
                row.createCell(11).setCellValue(trip.getWeightKg() != null ? trip.getWeightKg().doubleValue() : 0);
                row.createCell(12).setCellValue(trip.getNotes() != null ? trip.getNotes() : "");
                row.createCell(13).setCellValue(trip.getStatus().name());
                row.createCell(14).setCellValue(trip.getCreatedAt() != null ?
                        trip.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Error creating Excel export: {}", e.getMessage());
            throw new RuntimeException("Error creating Excel export", e);
        }
    }

    @Override
    public byte[] getCsvTemplate() {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", CSV_HEADERS)).append("\n");
        // Sample row
        csv.append("EWB123456789,Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,100000,12.5,30,1400,Electronics,5000,Sample notes\n");
        return csv.toString().getBytes();
    }

    @Override
    public byte[] getExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Trip Template");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"E-way Bill Number*", "Pickup*", "Destination*", "Sender*", "Receiver*",
                    "Transporter*", "Loan Amount*", "Interest Rate*", "Maturity Days*",
                    "Distance (km)", "Load Type", "Weight (kg)", "Notes"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("EWB123456789");
            sampleRow.createCell(1).setCellValue("Mumbai, Maharashtra");
            sampleRow.createCell(2).setCellValue("Delhi, NCR");
            sampleRow.createCell(3).setCellValue("ABC Traders");
            sampleRow.createCell(4).setCellValue("XYZ Industries");
            sampleRow.createCell(5).setCellValue("Fast Logistics Pvt Ltd");
            sampleRow.createCell(6).setCellValue(100000);
            sampleRow.createCell(7).setCellValue(12.5);
            sampleRow.createCell(8).setCellValue(30);
            sampleRow.createCell(9).setCellValue(1400.5);
            sampleRow.createCell(10).setCellValue("Electronics");
            sampleRow.createCell(11).setCellValue(5000);
            sampleRow.createCell(12).setCellValue("Handle with care");

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Instructions sheet
            Sheet instructionsSheet = workbook.createSheet("Instructions");
            instructionsSheet.createRow(0).createCell(0).setCellValue("BULK UPLOAD INSTRUCTIONS");
            instructionsSheet.createRow(2).createCell(0).setCellValue("1. Fields marked with * are required");
            instructionsSheet.createRow(3).createCell(0).setCellValue("2. E-way Bill Number must be unique");
            instructionsSheet.createRow(4).createCell(0).setCellValue("3. Loan Amount must be greater than 0");
            instructionsSheet.createRow(5).createCell(0).setCellValue("4. Interest Rate must be between 0 and 100");
            instructionsSheet.createRow(6).createCell(0).setCellValue("5. Maturity Days must be between 1 and 365");
            instructionsSheet.createRow(7).createCell(0).setCellValue("6. Distance and Weight are optional");

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Error creating Excel template: {}", e.getMessage());
            throw new RuntimeException("Error creating Excel template", e);
        }
    }

    // ==================== Statistics ====================

    @Override
    @Transactional(readOnly = true)
    public TripStatisticsDTO getTripStatistics() {
        log.debug("Calculating trip statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        // Get top locations/transporters
        Map<String, Long> topPickups = convertToMap(tripRepository.getTopPickupLocations(PageRequest.of(0, 10)));
        Map<String, Long> topDestinations = convertToMap(tripRepository.getTopDestinations(PageRequest.of(0, 10)));
        Map<String, Long> topTransporters = convertToMap(tripRepository.getTopTransporters(PageRequest.of(0, 10)));
        Map<String, Long> loadTypes = convertToMap(tripRepository.getTripCountByLoadType());

        // Calculate trips by status
        Map<String, Long> tripsByStatus = new LinkedHashMap<>();
        for (Trip.TripStatus status : Trip.TripStatus.values()) {
            tripsByStatus.put(status.name(), tripRepository.countByStatus(status));
        }

        // Calculate total interest amount
        BigDecimal totalLoanAmount = tripRepository.getTotalLoanAmount();
        BigDecimal avgInterestRate = tripRepository.getAverageInterestRate();
        Double avgMaturityDays = tripRepository.getAverageMaturityDays();
        BigDecimal totalInterest = BigDecimal.ZERO;
        if (totalLoanAmount != null && avgInterestRate != null && avgMaturityDays != null && avgMaturityDays > 0) {
            totalInterest = totalLoanAmount
                    .multiply(avgInterestRate)
                    .multiply(BigDecimal.valueOf(avgMaturityDays))
                    .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
        }

        return TripStatisticsDTO.builder()
                .totalTrips(tripRepository.count())
                .activeTrips(tripRepository.countByStatus(Trip.TripStatus.ACTIVE))
                .inTransitTrips(tripRepository.countByStatus(Trip.TripStatus.IN_TRANSIT))
                .completedTrips(tripRepository.countByStatus(Trip.TripStatus.COMPLETED))
                .cancelledTrips(tripRepository.countByStatus(Trip.TripStatus.CANCELLED))
                .totalLoanAmount(totalLoanAmount)
                .averageLoanAmount(tripRepository.getAverageLoanAmount())
                .averageInterestRate(avgInterestRate)
                .averageMaturityDays(avgMaturityDays)
                .totalDistanceKm(tripRepository.getTotalDistance())
                .totalWeightKg(tripRepository.getTotalWeight())
                .totalInterestAmount(totalInterest)
                .tripsCreatedToday(tripRepository.countByCreatedAtAfter(startOfToday))
                .tripsCreatedThisWeek(tripRepository.countByCreatedAtAfter(startOfWeek))
                .tripsCreatedThisMonth(tripRepository.countByCreatedAtAfter(startOfMonth))
                .tripsByStatus(tripsByStatus)
                .topPickupLocations(topPickups)
                .topDestinations(topDestinations)
                .topTransporters(topTransporters)
                .tripsByLoadType(loadTypes)
                .build();
    }

    // ==================== Document Operations ====================

    @Override
    public TripDocumentDTO.TripDocumentMetadataDTO uploadDocument(Long tripId, TripDocumentDTO documentDTO, Long userId) {
        log.debug("Uploading document for trip: {}, type: {}", tripId, documentDTO.getDocumentTypeCode());

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        DocumentType docType = documentTypeRepository.findByCode(documentDTO.getDocumentTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "code", documentDTO.getDocumentTypeCode()));

        byte[] documentData = null;
        if (StringUtils.hasText(documentDTO.getDocumentBase64())) {
            documentData = Base64.getDecoder().decode(
                    documentDTO.getDocumentBase64().replaceFirst("^data:[^;]+;base64,", ""));
        }

        TripDocument document = TripDocument.builder()
                .trip(trip)
                .documentType(docType)
                .documentName(documentDTO.getDocumentName())
                .documentData(documentData)
                .contentType(documentDTO.getContentType())
                .fileSize(documentData != null ? (long) documentData.length : null)
                .description(documentDTO.getDescription())
                .uploadedByUser(user)
                .build();

        TripDocument savedDocument = tripDocumentRepository.save(document);
        log.info("Document uploaded for trip {}: {}", tripId, savedDocument.getId());

        return mapToDocumentMetadataDTO(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDocumentDTO.TripDocumentMetadataDTO> getDocuments(Long tripId) {
        log.debug("Fetching documents for trip: {}", tripId);
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip", "id", tripId);
        }
        return tripDocumentRepository.findByTripId(tripId).stream()
                .map(this::mapToDocumentMetadataDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripDocumentDTO getDocumentByTypeCode(Long tripId, String documentTypeCode) {
        log.debug("Fetching document for trip: {}, type: {}", tripId, documentTypeCode);
        List<TripDocument> documents = tripDocumentRepository.findByTripIdAndDocumentTypeCodeOrderByCreatedAtDesc(tripId, documentTypeCode);
        if (documents.isEmpty()) {
            throw new ResourceNotFoundException("TripDocument", "tripId and type", tripId + "/" + documentTypeCode);
        }
        return mapToDocumentDTO(documents.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public TripDocumentDTO downloadDocument(Long documentId) {
        log.debug("Downloading document: {}", documentId);
        TripDocument document = tripDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("TripDocument", "id", documentId));
        return mapToDocumentDTO(document);
    }

    @Override
    public void deleteDocument(Long documentId) {
        log.debug("Deleting document: {}", documentId);
        if (!tripDocumentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("TripDocument", "id", documentId);
        }
        tripDocumentRepository.deleteById(documentId);
        log.info("Document deleted: {}", documentId);
    }

    // ==================== Validation ====================

    @Override
    @Transactional(readOnly = true)
    public boolean ewayBillNumberExists(String ewayBillNumber) {
        return tripRepository.existsByEwayBillNumber(ewayBillNumber);
    }

    // ==================== Helper Methods ====================

    private Trip buildTripFromRequest(TripRequestDTO request, User user) {
        return Trip.builder()
                .ewayBillNumber(request.getEwayBillNumber())
                .pickup(request.getPickup())
                .destination(request.getDestination())
                .sender(request.getSender())
                .receiver(request.getReceiver())
                .transporter(request.getTransporter())
                .loanAmount(request.getLoanAmount())
                .interestRate(request.getInterestRate())
                .maturityDays(request.getMaturityDays())
                .distanceKm(request.getDistanceKm())
                .loadType(request.getLoadType())
                .weightKg(request.getWeightKg())
                .notes(request.getNotes())
                .status(request.getStatus() != null ? request.getStatus() : Trip.TripStatus.ACTIVE)
                .createdByUser(user)
                .company(user.getCompany())
                .build();
    }

    private void updateTripFromRequest(Trip trip, TripRequestDTO request) {
        trip.setEwayBillNumber(request.getEwayBillNumber());
        trip.setPickup(request.getPickup());
        trip.setDestination(request.getDestination());
        trip.setSender(request.getSender());
        trip.setReceiver(request.getReceiver());
        trip.setTransporter(request.getTransporter());
        trip.setLoanAmount(request.getLoanAmount());
        trip.setInterestRate(request.getInterestRate());
        trip.setMaturityDays(request.getMaturityDays());
        trip.setDistanceKm(request.getDistanceKm());
        trip.setLoadType(request.getLoadType());
        trip.setWeightKg(request.getWeightKg());
        trip.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
        }
    }

    private TripResponseDTO mapToResponseDTO(Trip trip) {
        BigDecimal interestAmount = calculateInterestAmount(trip.getLoanAmount(), trip.getInterestRate(), trip.getMaturityDays());

        return TripResponseDTO.builder()
                .id(trip.getId())
                .ewayBillNumber(trip.getEwayBillNumber())
                .hasEwayBillImage(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(trip.getId(), DocumentType.CODE_EWAY_BILL))
                .pickup(trip.getPickup())
                .destination(trip.getDestination())
                .sender(trip.getSender())
                .receiver(trip.getReceiver())
                .transporter(trip.getTransporter())
                .loanAmount(trip.getLoanAmount())
                .interestRate(trip.getInterestRate())
                .maturityDays(trip.getMaturityDays())
                .distanceKm(trip.getDistanceKm())
                .loadType(trip.getLoadType())
                .weightKg(trip.getWeightKg())
                .notes(trip.getNotes())
                .status(trip.getStatus())
                .createdByUserId(trip.getCreatedByUser() != null ? trip.getCreatedByUser().getId() : null)
                .createdByUserName(trip.getCreatedByUser() != null ?
                        trip.getCreatedByUser().getFirstName() + " " + trip.getCreatedByUser().getLastName() : null)
                .companyId(trip.getCompany() != null ? trip.getCompany().getId() : null)
                .companyName(trip.getCompany() != null ? trip.getCompany().getName() : null)
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .totalInterestAmount(interestAmount)
                .totalAmountDue(trip.getLoanAmount().add(interestAmount))
                .build();
    }

    private BigDecimal calculateInterestAmount(BigDecimal loanAmount, BigDecimal interestRate, Integer maturityDays) {
        if (loanAmount == null || interestRate == null || maturityDays == null) {
            return BigDecimal.ZERO;
        }
        // Simple interest: P * R * T / 100, where T is in years
        return loanAmount
                .multiply(interestRate)
                .multiply(BigDecimal.valueOf(maturityDays))
                .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
    }

    private PagedResponse<TripResponseDTO> createPagedResponse(Page<Trip> tripPage) {
        List<TripResponseDTO> content = tripPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(tripPage, content);
    }

    private void uploadDocumentFromBase64(Trip trip, String documentTypeCode, String base64Data, String name, User user) {
        try {
            DocumentType docType = documentTypeRepository.findByCode(documentTypeCode)
                    .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "code", documentTypeCode));

            String cleanBase64 = base64Data.replaceFirst("^data:[^;]+;base64,", "");
            byte[] data = Base64.getDecoder().decode(cleanBase64);

            String contentType = "image/png"; // Default
            if (base64Data.startsWith("data:")) {
                int endIndex = base64Data.indexOf(";");
                if (endIndex > 5) {
                    contentType = base64Data.substring(5, endIndex);
                }
            }

            TripDocument document = TripDocument.builder()
                    .trip(trip)
                    .documentType(docType)
                    .documentName(name)
                    .documentData(data)
                    .contentType(contentType)
                    .fileSize((long) data.length)
                    .uploadedByUser(user)
                    .build();

            tripDocumentRepository.save(document);
        } catch (Exception e) {
            log.error("Error saving document: {}", e.getMessage());
        }
    }

    private TripDocumentDTO.TripDocumentMetadataDTO mapToDocumentMetadataDTO(TripDocument document) {
        return TripDocumentDTO.TripDocumentMetadataDTO.builder()
                .id(document.getId())
                .tripId(document.getTrip().getId())
                .documentTypeId(document.getDocumentType().getId())
                .documentTypeCode(document.getDocumentType().getCode())
                .documentTypeDisplayName(document.getDocumentType().getDisplayName())
                .documentName(document.getDocumentName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .description(document.getDescription())
                .uploadedByUserId(document.getUploadedByUser() != null ? document.getUploadedByUser().getId() : null)
                .uploadedByUserName(document.getUploadedByUser() != null ?
                        document.getUploadedByUser().getFirstName() + " " + document.getUploadedByUser().getLastName() : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private TripDocumentDTO mapToDocumentDTO(TripDocument document) {
        return TripDocumentDTO.builder()
                .id(document.getId())
                .tripId(document.getTrip().getId())
                .documentTypeId(document.getDocumentType().getId())
                .documentTypeCode(document.getDocumentType().getCode())
                .documentName(document.getDocumentName())
                .documentBase64(document.getDocumentData() != null ?
                        Base64.getEncoder().encodeToString(document.getDocumentData()) : null)
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .description(document.getDescription())
                .uploadedByUserId(document.getUploadedByUser() != null ? document.getUploadedByUser().getId() : null)
                .uploadedByUserName(document.getUploadedByUser() != null ?
                        document.getUploadedByUser().getFirstName() + " " + document.getUploadedByUser().getLastName() : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .hasData(document.getDocumentData() != null)
                .build();
    }

    private List<Trip> getTripsForExport(TripSearchCriteria criteria) {
        if (criteria != null) {
            Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
            return tripRepository.findAll(spec);
        }
        return tripRepository.findAll();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            if (row[0] != null) {
                map.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        return map;
    }
}
