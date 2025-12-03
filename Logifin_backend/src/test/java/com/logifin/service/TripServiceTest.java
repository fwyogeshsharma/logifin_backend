package com.logifin.service;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.DocumentTypeRepository;
import com.logifin.repository.TripDocumentRepository;
import com.logifin.repository.TripRepository;
import com.logifin.repository.UserRepository;
import com.logifin.repository.specification.TripSpecification;
import com.logifin.service.impl.TripServiceImpl;
import com.logifin.util.TripExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripService Tests")
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripDocumentRepository tripDocumentRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripExcelParser tripExcelParser;

    @InjectMocks
    private TripServiceImpl tripService;

    private User testUser;
    private Company testCompany;
    private Trip testTrip;
    private TripRequestDTO testTripRequestDTO;
    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .name("Test Company")
                .build();
        testCompany.setId(1L);

        Role testRole = Role.builder()
                .roleName("ROLE_TRANSPORTER")
                .description("Transporter Role")
                .build();
        testRole.setId(1L);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .company(testCompany)
                .role(testRole)
                .active(true)
                .build();
        testUser.setId(1L);

        testTrip = Trip.builder()
                .ewayBillNumber("EWB123456789")
                .pickup("Mumbai")
                .destination("Delhi")
                .sender("ABC Traders")
                .receiver("XYZ Industries")
                .transporter("Fast Logistics")
                .loanAmount(new BigDecimal("100000"))
                .interestRate(new BigDecimal("12.5"))
                .maturityDays(30)
                .distanceKm(new BigDecimal("1400"))
                .loadType("Electronics")
                .weightKg(new BigDecimal("5000"))
                .notes("Handle with care")
                .status(Trip.TripStatus.ACTIVE)
                .createdByUser(testUser)
                .company(testCompany)
                .build();
        testTrip.setId(1L);
        testTrip.setCreatedAt(LocalDateTime.now());
        testTrip.setUpdatedAt(LocalDateTime.now());

        testTripRequestDTO = TripRequestDTO.builder()
                .ewayBillNumber("EWB123456789")
                .pickup("Mumbai")
                .destination("Delhi")
                .sender("ABC Traders")
                .receiver("XYZ Industries")
                .transporter("Fast Logistics")
                .loanAmount(new BigDecimal("100000"))
                .interestRate(new BigDecimal("12.5"))
                .maturityDays(30)
                .distanceKm(new BigDecimal("1400"))
                .loadType("Electronics")
                .weightKg(new BigDecimal("5000"))
                .notes("Handle with care")
                .build();

        testDocumentType = DocumentType.builder()
                .code("EWAY_BILL")
                .displayName("E-Way Bill")
                .isActive(true)
                .build();
        testDocumentType.setId(1L);
    }

    @Nested
    @DisplayName("Create Trip Tests")
    class CreateTripTests {

        @Test
        @DisplayName("Should create trip successfully")
        void createTrip_Success() {
            when(tripRepository.existsByEwayBillNumber(anyString())).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            TripResponseDTO result = tripService.createTrip(testTripRequestDTO, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getEwayBillNumber()).isEqualTo("EWB123456789");
            assertThat(result.getPickup()).isEqualTo("Mumbai");
            assertThat(result.getDestination()).isEqualTo("Delhi");
            verify(tripRepository).save(any(Trip.class));
        }

        @Test
        @DisplayName("Should throw exception when E-way Bill Number already exists")
        void createTrip_DuplicateEwayBillNumber() {
            when(tripRepository.existsByEwayBillNumber("EWB123456789")).thenReturn(true);

            assertThatThrownBy(() -> tripService.createTrip(testTripRequestDTO, 1L))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("ewayBillNumber");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createTrip_UserNotFound() {
            when(tripRepository.existsByEwayBillNumber(anyString())).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.createTrip(testTripRequestDTO, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Should create trip with E-way Bill image")
        void createTrip_WithEwayBillImage() {
            testTripRequestDTO.setEwayBillImageBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

            when(tripRepository.existsByEwayBillNumber(anyString())).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);
            when(documentTypeRepository.findByCode("EWAY_BILL")).thenReturn(Optional.of(testDocumentType));
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            TripResponseDTO result = tripService.createTrip(testTripRequestDTO, 1L);

            assertThat(result).isNotNull();
            verify(tripDocumentRepository).save(any(TripDocument.class));
        }
    }

    @Nested
    @DisplayName("Get Trip Tests")
    class GetTripTests {

        @Test
        @DisplayName("Should get trip by ID successfully")
        void getTripById_Success() {
            when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(1L, "EWAY_BILL")).thenReturn(true);

            TripResponseDTO result = tripService.getTripById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEwayBillNumber()).isEqualTo("EWB123456789");
            assertThat(result.getHasEwayBillImage()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when trip not found")
        void getTripById_NotFound() {
            when(tripRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.getTripById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Trip");
        }
    }

    @Nested
    @DisplayName("Update Trip Tests")
    class UpdateTripTests {

        @Test
        @DisplayName("Should update trip successfully")
        void updateTrip_Success() {
            TripRequestDTO updateRequest = TripRequestDTO.builder()
                    .ewayBillNumber("EWB123456789")
                    .pickup("Chennai")
                    .destination("Bangalore")
                    .sender("ABC Traders")
                    .receiver("XYZ Industries")
                    .transporter("Fast Logistics")
                    .loanAmount(new BigDecimal("150000"))
                    .interestRate(new BigDecimal("10.0"))
                    .maturityDays(45)
                    .build();

            when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
            when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            TripResponseDTO result = tripService.updateTrip(1L, updateRequest, 1L);

            assertThat(result).isNotNull();
            verify(tripRepository).save(any(Trip.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent trip")
        void updateTrip_NotFound() {
            when(tripRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.updateTrip(999L, testTripRequestDTO, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Trip");
        }

        @Test
        @DisplayName("Should throw exception when changing to existing E-way Bill Number")
        void updateTrip_DuplicateEwayBillNumber() {
            TripRequestDTO updateRequest = TripRequestDTO.builder()
                    .ewayBillNumber("EWB999999999")
                    .pickup("Mumbai")
                    .destination("Delhi")
                    .sender("ABC Traders")
                    .receiver("XYZ Industries")
                    .transporter("Fast Logistics")
                    .loanAmount(new BigDecimal("100000"))
                    .interestRate(new BigDecimal("12.5"))
                    .maturityDays(30)
                    .build();

            when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
            when(tripRepository.existsByEwayBillNumber("EWB999999999")).thenReturn(true);

            assertThatThrownBy(() -> tripService.updateTrip(1L, updateRequest, 1L))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete Trip Tests")
    class DeleteTripTests {

        @Test
        @DisplayName("Should delete trip successfully")
        void deleteTrip_Success() {
            when(tripRepository.existsById(1L)).thenReturn(true);
            doNothing().when(tripRepository).deleteById(1L);

            tripService.deleteTrip(1L);

            verify(tripRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent trip")
        void deleteTrip_NotFound() {
            when(tripRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> tripService.deleteTrip(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Trip");
        }
    }

    @Nested
    @DisplayName("Search and Pagination Tests")
    class SearchTests {

        @Test
        @DisplayName("Should get all trips with pagination")
        void getAllTrips_Success() {
            List<Trip> trips = Arrays.asList(testTrip);
            Page<Trip> tripPage = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

            when(tripRepository.findAll(any(Pageable.class))).thenReturn(tripPage);
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            PagedResponse<TripResponseDTO> result = tripService.getAllTrips(PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should search trips with criteria")
        void searchTrips_Success() {
            List<Trip> trips = Arrays.asList(testTrip);
            Page<Trip> tripPage = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .transporter("Fast Logistics")
                    .status(Trip.TripStatus.ACTIVE)
                    .build();

            when(tripRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tripPage);
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            PagedResponse<TripResponseDTO> result = tripService.searchTrips(criteria, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should search trips by keyword")
        void searchByKeyword_Success() {
            List<Trip> trips = Arrays.asList(testTrip);
            Page<Trip> tripPage = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

            when(tripRepository.searchByKeyword(eq("Mumbai"), any(Pageable.class))).thenReturn(tripPage);
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            PagedResponse<TripResponseDTO> result = tripService.searchByKeyword("Mumbai", PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get trip statistics")
        void getTripStatistics_Success() {
            when(tripRepository.count()).thenReturn(100L);
            when(tripRepository.countByStatus(Trip.TripStatus.ACTIVE)).thenReturn(40L);
            when(tripRepository.countByStatus(Trip.TripStatus.IN_TRANSIT)).thenReturn(30L);
            when(tripRepository.countByStatus(Trip.TripStatus.COMPLETED)).thenReturn(25L);
            when(tripRepository.countByStatus(Trip.TripStatus.CANCELLED)).thenReturn(5L);
            when(tripRepository.getTotalLoanAmount()).thenReturn(new BigDecimal("10000000"));
            when(tripRepository.getAverageLoanAmount()).thenReturn(new BigDecimal("100000"));
            when(tripRepository.getAverageInterestRate()).thenReturn(new BigDecimal("12.0"));
            when(tripRepository.getAverageMaturityDays()).thenReturn(30.0);
            when(tripRepository.getTotalDistance()).thenReturn(new BigDecimal("140000"));
            when(tripRepository.getTotalWeight()).thenReturn(new BigDecimal("500000"));
            when(tripRepository.countByCreatedAtAfter(any())).thenReturn(10L);
            when(tripRepository.getTopPickupLocations(any())).thenReturn(new ArrayList<>());
            when(tripRepository.getTopDestinations(any())).thenReturn(new ArrayList<>());
            when(tripRepository.getTopTransporters(any())).thenReturn(new ArrayList<>());
            when(tripRepository.getTripCountByLoadType()).thenReturn(new ArrayList<>());

            TripStatisticsDTO result = tripService.getTripStatistics();

            assertThat(result).isNotNull();
            assertThat(result.getTotalTrips()).isEqualTo(100L);
            assertThat(result.getActiveTrips()).isEqualTo(40L);
            assertThat(result.getCompletedTrips()).isEqualTo(25L);
        }
    }

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export trips to CSV")
        void exportTripsToCsv_Success() {
            when(tripRepository.findAll()).thenReturn(Arrays.asList(testTrip));

            byte[] result = tripService.exportTripsToCsv(null);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
            String csv = new String(result);
            assertThat(csv).contains("ewayBillNumber");
            assertThat(csv).contains("EWB123456789");
        }

        @Test
        @DisplayName("Should export trips to Excel")
        void exportTripsToExcel_Success() {
            when(tripRepository.findAll()).thenReturn(Arrays.asList(testTrip));

            byte[] result = tripService.exportTripsToExcel(null);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should get CSV template")
        void getCsvTemplate_Success() {
            byte[] result = tripService.getCsvTemplate();

            assertThat(result).isNotNull();
            String template = new String(result);
            assertThat(template).contains("ewayBillNumber");
            assertThat(template).contains("pickup");
            assertThat(template).contains("destination");
        }

        @Test
        @DisplayName("Should get Excel template")
        void getExcelTemplate_Success() {
            byte[] result = tripService.getExcelTemplate();

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Document Operations Tests")
    class DocumentTests {

        private TripDocument testDocument;

        @BeforeEach
        void setUpDocument() {
            testDocument = TripDocument.builder()
                    .trip(testTrip)
                    .documentType(testDocumentType)
                    .documentName("eway_bill.pdf")
                    .contentType("application/pdf")
                    .fileSize(1024L)
                    .documentData("test data".getBytes())
                    .uploadedByUser(testUser)
                    .build();
            testDocument.setId(1L);
            testDocument.setCreatedAt(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should upload document successfully")
        void uploadDocument_Success() {
            TripDocumentDTO documentDTO = TripDocumentDTO.builder()
                    .documentTypeCode("EWAY_BILL")
                    .documentName("eway_bill.pdf")
                    .contentType("application/pdf")
                    .documentBase64("dGVzdCBkYXRh")
                    .build();

            when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(documentTypeRepository.findByCode("EWAY_BILL")).thenReturn(Optional.of(testDocumentType));
            when(tripDocumentRepository.save(any(TripDocument.class))).thenReturn(testDocument);

            TripDocumentDTO.TripDocumentMetadataDTO result = tripService.uploadDocument(1L, documentDTO, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getDocumentTypeCode()).isEqualTo("EWAY_BILL");
            verify(tripDocumentRepository).save(any(TripDocument.class));
        }

        @Test
        @DisplayName("Should get documents for trip")
        void getDocuments_Success() {
            when(tripRepository.existsById(1L)).thenReturn(true);
            when(tripDocumentRepository.findByTripId(1L)).thenReturn(Arrays.asList(testDocument));

            List<TripDocumentDTO.TripDocumentMetadataDTO> result = tripService.getDocuments(1L);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when getting documents for non-existent trip")
        void getDocuments_TripNotFound() {
            when(tripRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> tripService.getDocuments(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Trip");
        }

        @Test
        @DisplayName("Should download document successfully")
        void downloadDocument_Success() {
            when(tripDocumentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

            TripDocumentDTO result = tripService.downloadDocument(1L);

            assertThat(result).isNotNull();
            assertThat(result.getDocumentBase64()).isNotNull();
        }

        @Test
        @DisplayName("Should delete document successfully")
        void deleteDocument_Success() {
            when(tripDocumentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(tripDocumentRepository).deleteById(1L);

            tripService.deleteDocument(1L);

            verify(tripDocumentRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return true when E-way Bill Number exists")
        void ewayBillNumberExists_True() {
            when(tripRepository.existsByEwayBillNumber("EWB123456789")).thenReturn(true);

            boolean result = tripService.ewayBillNumberExists("EWB123456789");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when E-way Bill Number does not exist")
        void ewayBillNumberExists_False() {
            when(tripRepository.existsByEwayBillNumber("EWB999999999")).thenReturn(false);

            boolean result = tripService.ewayBillNumberExists("EWB999999999");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Interest Calculation Tests")
    class InterestCalculationTests {

        @Test
        @DisplayName("Should calculate total interest amount correctly")
        void calculateInterest_Success() {
            when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
            when(tripDocumentRepository.existsByTripIdAndDocumentTypeCode(anyLong(), anyString())).thenReturn(false);

            TripResponseDTO result = tripService.getTripById(1L);

            // Interest = 100000 * 12.5 * 30 / 36500 = 1027.40 (approx)
            assertThat(result.getTotalInterestAmount()).isNotNull();
            assertThat(result.getTotalAmountDue()).isGreaterThan(result.getLoanAmount());
        }
    }
}
