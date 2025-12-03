package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.*;
import com.logifin.entity.Trip;
import com.logifin.security.JwtAuthenticationFilter;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@DisplayName("TripController Tests")
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private TripRequestDTO testTripRequestDTO;
    private TripResponseDTO testTripResponseDTO;
    private UserPrincipal transporterPrincipal;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
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

        testTripResponseDTO = TripResponseDTO.builder()
                .id(1L)
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
                .hasEwayBillImage(false)
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
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CSR")))
                .build();
    }

    @Nested
    @DisplayName("Create Trip Tests")
    class CreateTripTests {

        @Test
        @DisplayName("Should create trip with TRANSPORTER role")
        @WithMockUser(roles = "TRANSPORTER")
        void createTrip_Success() throws Exception {
            when(tripService.createTrip(any(TripRequestDTO.class), anyLong()))
                    .thenReturn(testTripResponseDTO);

            mockMvc.perform(post("/api/v1/trip")
                            .with(csrf())
                            .with(user(transporterPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTripRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.ewayBillNumber").value("EWB123456789"));
        }

        @Test
        @DisplayName("Should reject create trip without TRANSPORTER role")
        @WithMockUser(roles = "CSR")
        void createTrip_Forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/trip")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTripRequestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject create trip with invalid data")
        @WithMockUser(roles = "TRANSPORTER")
        void createTrip_ValidationError() throws Exception {
            TripRequestDTO invalidRequest = TripRequestDTO.builder()
                    .ewayBillNumber("") // Invalid - empty
                    .pickup("Mumbai")
                    .destination("Delhi")
                    .build();

            mockMvc.perform(post("/api/v1/trip")
                            .with(csrf())
                            .with(user(transporterPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Trip Tests")
    class GetTripTests {

        @Test
        @DisplayName("Should get trip by ID")
        @WithMockUser
        void getTripById_Success() throws Exception {
            when(tripService.getTripById(1L)).thenReturn(testTripResponseDTO);

            mockMvc.perform(get("/api/v1/trip/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.ewayBillNumber").value("EWB123456789"));
        }

        @Test
        @DisplayName("Should get all trips with pagination")
        @WithMockUser
        void getAllTrips_Success() throws Exception {
            List<TripResponseDTO> trips = Arrays.asList(testTripResponseDTO);
            PagedResponse<TripResponseDTO> pagedResponse = PagedResponse.<TripResponseDTO>builder()
                    .content(trips)
                    .page(0)
                    .size(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(tripService.searchTrips(any(TripSearchCriteria.class), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/trips")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("Should search trips with filters")
        @WithMockUser
        void searchTrips_WithFilters() throws Exception {
            List<TripResponseDTO> trips = Arrays.asList(testTripResponseDTO);
            PagedResponse<TripResponseDTO> pagedResponse = PagedResponse.<TripResponseDTO>builder()
                    .content(trips)
                    .page(0)
                    .size(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(tripService.searchTrips(any(TripSearchCriteria.class), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/trips")
                            .param("transporter", "Fast Logistics")
                            .param("status", "ACTIVE")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("Update Trip Tests")
    class UpdateTripTests {

        @Test
        @DisplayName("Should update trip with TRANSPORTER role")
        @WithMockUser(roles = "TRANSPORTER")
        void updateTrip_Success() throws Exception {
            when(tripService.updateTrip(eq(1L), any(TripRequestDTO.class), anyLong()))
                    .thenReturn(testTripResponseDTO);

            mockMvc.perform(put("/api/v1/trip/1")
                            .with(csrf())
                            .with(user(transporterPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTripRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.ewayBillNumber").value("EWB123456789"));
        }

        @Test
        @DisplayName("Should reject update without TRANSPORTER role")
        @WithMockUser(roles = "CSR")
        void updateTrip_Forbidden() throws Exception {
            mockMvc.perform(put("/api/v1/trip/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTripRequestDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Delete Trip Tests")
    class DeleteTripTests {

        @Test
        @DisplayName("Should delete trip with TRANSPORTER role")
        @WithMockUser(roles = "TRANSPORTER")
        void deleteTrip_Success() throws Exception {
            doNothing().when(tripService).deleteTrip(1L);

            mockMvc.perform(delete("/api/v1/trip/1")
                            .with(csrf())
                            .with(user(transporterPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Trip deleted successfully"));
        }

        @Test
        @DisplayName("Should reject delete without TRANSPORTER role")
        @WithMockUser(roles = "CSR")
        void deleteTrip_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/trip/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Bulk Upload Tests")
    class BulkUploadTests {

        @Test
        @DisplayName("Should bulk upload trips with TRANSPORTER role")
        @WithMockUser(roles = "TRANSPORTER")
        void bulkUpload_Success() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    "ewayBillNumber,pickup,destination\nEWB123,Mumbai,Delhi".getBytes()
            );

            BulkUploadResponseDTO response = BulkUploadResponseDTO.builder()
                    .successCount(1)
                    .failureCount(0)
                    .successfulTripIds(Arrays.asList(1L))
                    .errors(new ArrayList<>())
                    .build();
            response.calculateSummary();

            when(tripService.bulkUploadTrips(any(), anyLong())).thenReturn(response);

            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/trip/upload")
                            .file(file)
                            .with(csrf())
                            .with(user(transporterPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.successCount").value(1));
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get trip statistics")
        @WithMockUser
        void getStatistics_Success() throws Exception {
            TripStatisticsDTO statistics = TripStatisticsDTO.builder()
                    .totalTrips(100L)
                    .activeTrips(40L)
                    .inTransitTrips(30L)
                    .completedTrips(25L)
                    .cancelledTrips(5L)
                    .totalLoanAmount(new BigDecimal("10000000"))
                    .build();

            when(tripService.getTripStatistics()).thenReturn(statistics);

            mockMvc.perform(get("/api/v1/trips/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalTrips").value(100))
                    .andExpect(jsonPath("$.data.activeTrips").value(40));
        }
    }

    @Nested
    @DisplayName("Template Download Tests")
    class TemplateTests {

        @Test
        @DisplayName("Should download CSV template")
        @WithMockUser
        void downloadCsvTemplate_Success() throws Exception {
            byte[] templateContent = "ewayBillNumber,pickup,destination".getBytes();
            when(tripService.getCsvTemplate()).thenReturn(templateContent);

            mockMvc.perform(get("/api/v1/trip/template/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=trip_template.csv"))
                    .andExpect(content().contentType("text/csv"));
        }

        @Test
        @DisplayName("Should download Excel template")
        @WithMockUser
        void downloadExcelTemplate_Success() throws Exception {
            byte[] templateContent = new byte[]{0x50, 0x4B, 0x03, 0x04}; // XLSX signature
            when(tripService.getExcelTemplate()).thenReturn(templateContent);

            mockMvc.perform(get("/api/v1/trip/template/excel"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=trip_template.xlsx"));
        }
    }

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export trips to CSV")
        @WithMockUser
        void exportToCsv_Success() throws Exception {
            byte[] csvContent = "ewayBillNumber,pickup,destination\nEWB123,Mumbai,Delhi".getBytes();
            when(tripService.exportTripsToCsv(any())).thenReturn(csvContent);

            mockMvc.perform(get("/api/v1/trips/export/csv"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/csv"));
        }

        @Test
        @DisplayName("Should export trips to Excel")
        @WithMockUser
        void exportToExcel_Success() throws Exception {
            byte[] excelContent = new byte[]{0x50, 0x4B, 0x03, 0x04};
            when(tripService.exportTripsToExcel(any())).thenReturn(excelContent);

            mockMvc.perform(get("/api/v1/trips/export/excel"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Document Operations Tests")
    class DocumentTests {

        @Test
        @DisplayName("Should upload document with TRANSPORTER role")
        @WithMockUser(roles = "TRANSPORTER")
        void uploadDocument_Success() throws Exception {
            TripDocumentDTO documentDTO = TripDocumentDTO.builder()
                    .documentTypeCode("EWAY_BILL")
                    .documentName("eway_bill.pdf")
                    .contentType("application/pdf")
                    .documentBase64("dGVzdCBkYXRh")
                    .build();

            TripDocumentDTO.TripDocumentMetadataDTO metadataDTO = TripDocumentDTO.TripDocumentMetadataDTO.builder()
                    .id(1L)
                    .tripId(1L)
                    .documentTypeCode("EWAY_BILL")
                    .documentTypeDisplayName("E-Way Bill")
                    .documentName("eway_bill.pdf")
                    .build();

            when(tripService.uploadDocument(eq(1L), any(TripDocumentDTO.class), anyLong()))
                    .thenReturn(metadataDTO);

            mockMvc.perform(post("/api/v1/trip/1/document")
                            .with(csrf())
                            .with(user(transporterPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(documentDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.documentTypeCode").value("EWAY_BILL"));
        }

        @Test
        @DisplayName("Should get trip documents")
        @WithMockUser
        void getTripDocuments_Success() throws Exception {
            List<TripDocumentDTO.TripDocumentMetadataDTO> documents = Arrays.asList(
                    TripDocumentDTO.TripDocumentMetadataDTO.builder()
                            .id(1L)
                            .tripId(1L)
                            .documentTypeCode("EWAY_BILL")
                            .documentTypeDisplayName("E-Way Bill")
                            .documentName("eway_bill.pdf")
                            .build()
            );

            when(tripService.getDocuments(1L)).thenReturn(documents);

            mockMvc.perform(get("/api/v1/trip/1/documents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].documentTypeCode").value("EWAY_BILL"));
        }

        @Test
        @DisplayName("Should get document by type")
        @WithMockUser
        void getDocumentByType_Success() throws Exception {
            TripDocumentDTO documentDTO = TripDocumentDTO.builder()
                    .id(1L)
                    .tripId(1L)
                    .documentTypeCode("EWAY_BILL")
                    .documentName("eway_bill.pdf")
                    .documentBase64("dGVzdCBkYXRh")
                    .build();

            when(tripService.getDocumentByTypeCode(1L, "EWAY_BILL")).thenReturn(documentDTO);

            mockMvc.perform(get("/api/v1/trip/1/document/EWAY_BILL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.documentTypeCode").value("EWAY_BILL"));
        }

        @Test
        @DisplayName("Should download document")
        @WithMockUser
        void downloadDocument_Success() throws Exception {
            TripDocumentDTO documentDTO = TripDocumentDTO.builder()
                    .id(1L)
                    .documentName("eway_bill.pdf")
                    .contentType("application/pdf")
                    .documentBase64("dGVzdCBkYXRh")
                    .build();

            when(tripService.downloadDocument(1L)).thenReturn(documentDTO);

            mockMvc.perform(get("/api/v1/trip/document/1/download"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=eway_bill.pdf"));
        }

        @Test
        @DisplayName("Should delete document with TRANSPORTER role")
        @WithMockUser(roles = "TRANSPORTER")
        void deleteDocument_Success() throws Exception {
            doNothing().when(tripService).deleteDocument(1L);

            mockMvc.perform(delete("/api/v1/trip/document/1")
                            .with(csrf())
                            .with(user(transporterPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Document deleted successfully"));
        }
    }

    @Nested
    @DisplayName("Validation Endpoint Tests")
    class ValidationEndpointTests {

        @Test
        @DisplayName("Should check E-way Bill exists")
        @WithMockUser
        void checkEwayBillExists_True() throws Exception {
            when(tripService.ewayBillNumberExists("EWB123456789")).thenReturn(true);

            mockMvc.perform(get("/api/v1/trip/check-eway-bill/EWB123456789"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("Should check E-way Bill does not exist")
        @WithMockUser
        void checkEwayBillExists_False() throws Exception {
            when(tripService.ewayBillNumberExists("EWB999999999")).thenReturn(false);

            mockMvc.perform(get("/api/v1/trip/check-eway-bill/EWB999999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }
}
