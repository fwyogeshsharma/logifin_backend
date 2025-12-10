package com.logifin.service;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.DocumentTypeRepository;
import com.logifin.repository.TripDocumentRepository;
import com.logifin.repository.TripRepository;
import com.logifin.repository.UserRepository;
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
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);
            when(tripDocumentRepository.findByTripId(anyLong())).thenReturn(Collections.emptyList());

            TripResponseDTO result = tripService.createTrip(testTripRequestDTO, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getPickup()).isEqualTo("Mumbai");
            assertThat(result.getDestination()).isEqualTo("Delhi");
            verify(tripRepository).save(any(Trip.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createTrip_UserNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.createTrip(testTripRequestDTO, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Should create trip with documents")
        void createTrip_WithDocuments() {
            DocumentUploadDTO docDTO = DocumentUploadDTO.builder()
                    .documentTypeId(1L)
                    .documentNumber("EWB123456789")
                    .documentBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
                    .build();
            testTripRequestDTO.setDocuments(Collections.singletonList(docDTO));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);
            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(tripDocumentRepository.findByTripId(anyLong())).thenReturn(Collections.emptyList());

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
            when(tripDocumentRepository.findByTripId(1L)).thenReturn(Collections.emptyList());

            TripResponseDTO result = tripService.getTripById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getPickup()).isEqualTo("Mumbai");
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
            when(tripDocumentRepository.findByTripId(anyLong())).thenReturn(Collections.emptyList());

            TripResponseDTO result = tripService.updateTrip(1L, updateRequest, 1L);

            assertThat(result).isNotNull();
            verify(tripRepository).save(any(Trip.class));
        }

        @Test
        @DisplayName("Should throw exception when trip not found")
        void updateTrip_NotFound() {
            when(tripRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tripService.updateTrip(999L, testTripRequestDTO, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Trip");
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
        @DisplayName("Should throw exception when trip not found")
        void deleteTrip_NotFound() {
            when(tripRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> tripService.deleteTrip(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Trip");
        }
    }

    @Nested
    @DisplayName("Search Trip Tests")
    class SearchTripTests {

        @Test
        @DisplayName("Should get all trips with pagination")
        void getAllTrips_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Trip> tripPage = new PageImpl<>(Collections.singletonList(testTrip), pageable, 1);

            when(tripRepository.findAll(pageable)).thenReturn(tripPage);
            when(tripDocumentRepository.findByTripId(anyLong())).thenReturn(Collections.emptyList());

            PagedResponse<TripResponseDTO> result = tripService.getAllTrips(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should search trips with criteria")
        @SuppressWarnings("unchecked")
        void searchTrips_WithCriteria() {
            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .pickup("Mumbai")
                    .status(Trip.TripStatus.ACTIVE)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Trip> tripPage = new PageImpl<>(Collections.singletonList(testTrip), pageable, 1);

            when(tripRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tripPage);
            when(tripDocumentRepository.findByTripId(anyLong())).thenReturn(Collections.emptyList());

            PagedResponse<TripResponseDTO> result = tripService.searchTrips(criteria, pageable);

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
            when(tripRepository.countByStatus(Trip.TripStatus.ACTIVE)).thenReturn(60L);
            when(tripRepository.countByStatus(Trip.TripStatus.IN_TRANSIT)).thenReturn(20L);
            when(tripRepository.countByStatus(Trip.TripStatus.COMPLETED)).thenReturn(15L);
            when(tripRepository.countByStatus(Trip.TripStatus.CANCELLED)).thenReturn(5L);
            when(tripRepository.getTotalLoanAmount()).thenReturn(new BigDecimal("5000000"));
            when(tripRepository.getAverageLoanAmount()).thenReturn(new BigDecimal("50000"));
            when(tripRepository.getAverageInterestRate()).thenReturn(new BigDecimal("10.5"));
            when(tripRepository.getAverageMaturityDays()).thenReturn(30.0);
            when(tripRepository.countByCreatedAtAfter(any())).thenReturn(10L);
            when(tripRepository.getTopPickupLocations(any())).thenReturn(Collections.emptyList());
            when(tripRepository.getTopDestinations(any())).thenReturn(Collections.emptyList());
            when(tripRepository.getTopTransporters(any())).thenReturn(Collections.emptyList());
            when(tripRepository.getTripCountByLoadType()).thenReturn(Collections.emptyList());
            when(tripRepository.getTotalDistance()).thenReturn(new BigDecimal("100000"));
            when(tripRepository.getTotalWeight()).thenReturn(new BigDecimal("500000"));

            TripStatisticsDTO result = tripService.getTripStatistics();

            assertThat(result).isNotNull();
            assertThat(result.getTotalTrips()).isEqualTo(100L);
            assertThat(result.getActiveTrips()).isEqualTo(60L);
            assertThat(result.getTotalLoanAmount()).isEqualByComparingTo(new BigDecimal("5000000"));
        }
    }
}
