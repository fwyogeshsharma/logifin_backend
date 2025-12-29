package com.logifin.controller;

import com.logifin.dto.*;
import com.logifin.entity.Trip;
import com.logifin.security.UserPrincipal;
import com.logifin.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripController Tests")
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private TripRequestDTO testTripRequestDTO;
    private TripResponseDTO testTripResponseDTO;
    private UserPrincipal transporterPrincipal;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testTripRequestDTO = TripRequestDTO.builder()
                .pickup("Mumbai")
                .destination("Delhi")
                .senderId(2L)
                .receiver("XYZ Industries")
                .transporterId(3L)
                .loanAmount(new BigDecimal("100000"))
                .interestRate(new BigDecimal("12.5"))
                .maturityDays(30)
                .distanceKm(new BigDecimal("1400"))
                .loadType("Electronics")
                .weightKg(new BigDecimal("5000"))
                .notes("Handle with care")
                .build();

        testTripResponseDTO = TripResponseDTO.builder()
                .id(1L)
                .pickup("Mumbai")
                .destination("Delhi")
                .senderId(2L)
                .senderName("Sender User")
                .receiver("XYZ Industries")
                .transporterId(3L)
                .transporterName("Transporter User")
                .loanAmount(new BigDecimal("100000"))
                .interestRate(new BigDecimal("12.5"))
                .maturityDays(30)
                .distanceKm(new BigDecimal("1400"))
                .loadType("Electronics")
                .weightKg(new BigDecimal("5000"))
                .notes("Handle with care")
                .status(Trip.TripStatus.ACTIVE)
                .documents(Collections.emptyList())
                .totalInterestAmount(new BigDecimal("1027.40"))
                .totalAmountDue(new BigDecimal("101027.40"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transporterPrincipal = UserPrincipal.builder()
                .id(1L)
                .firstName("John")
                .lastName("Transporter")
                .email("transporter@test.com")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSPORTER")))
                .build();

        userPrincipal = UserPrincipal.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("User")
                .email("user@test.com")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    @DisplayName("Should create trip successfully")
    void createTrip_Success() {
        when(tripService.createTrip(any(TripRequestDTO.class), eq(1L))).thenReturn(testTripResponseDTO);

        ResponseEntity<ApiResponse<TripResponseDTO>> response = tripController.createTrip(testTripRequestDTO, transporterPrincipal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getPickup()).isEqualTo("Mumbai");
        verify(tripService).createTrip(any(TripRequestDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Should get trip by ID successfully")
    void getTripById_Success() {
        when(tripService.getTripById(1L)).thenReturn(testTripResponseDTO);

        ResponseEntity<ApiResponse<TripResponseDTO>> response = tripController.getTripById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
        verify(tripService).getTripById(1L);
    }

    @Test
    @DisplayName("Should update trip successfully")
    void updateTrip_Success() {
        when(tripService.updateTrip(eq(1L), any(TripRequestDTO.class), eq(1L))).thenReturn(testTripResponseDTO);

        ResponseEntity<ApiResponse<TripResponseDTO>> response = tripController.updateTrip(1L, testTripRequestDTO, transporterPrincipal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(tripService).updateTrip(eq(1L), any(TripRequestDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Should delete trip successfully")
    void deleteTrip_Success() {
        doNothing().when(tripService).deleteTrip(1L);

        ResponseEntity<ApiResponse<Void>> response = tripController.deleteTrip(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(tripService).deleteTrip(1L);
    }

    @Test
    @DisplayName("Should get all trips with createdByUserId filter")
    void getAllTrips_WithCreatedByUserIdFilter() {
        PagedResponse<TripResponseDTO> pagedResponse = PagedResponse.<TripResponseDTO>builder()
                .content(Collections.singletonList(testTripResponseDTO))
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        when(tripService.searchTrips(any(TripSearchCriteria.class), any())).thenReturn(pagedResponse);

        ResponseEntity<ApiResponse<PagedResponse<TripResponseDTO>>> response = tripController.getAllTrips(
                null, null, null, null, null, null, null, null, null, null,
                1L, // createdByUserId
                0, 10, "createdAt", "desc"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(tripService).searchTrips(argThat(criteria ->
            criteria.getCreatedByUserId() != null && criteria.getCreatedByUserId().equals(1L)
        ), any());
    }

    @Test
    @DisplayName("Should get all trips without createdByUserId filter")
    void getAllTrips_WithoutCreatedByUserIdFilter() {
        PagedResponse<TripResponseDTO> pagedResponse = PagedResponse.<TripResponseDTO>builder()
                .content(Collections.emptyList())
                .totalElements(0L)
                .totalPages(0)
                .page(0)
                .size(10)
                .build();

        when(tripService.searchTrips(any(TripSearchCriteria.class), any())).thenReturn(pagedResponse);

        ResponseEntity<ApiResponse<PagedResponse<TripResponseDTO>>> response = tripController.getAllTrips(
                null, null, null, null, null, null, null, null, null, null,
                null, // createdByUserId is null
                0, 10, "createdAt", "desc"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();

        verify(tripService).searchTrips(argThat(criteria ->
            criteria.getCreatedByUserId() == null
        ), any());
    }
}
