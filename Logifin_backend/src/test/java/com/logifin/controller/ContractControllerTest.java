package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.*;
import com.logifin.security.JwtAuthenticationFilter;
import com.logifin.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContractController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("ContractController Tests")
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContractService contractService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ContractResponse testContractResponse;
    private CreateContractRequest createRequest;
    private UpdateContractRequest updateRequest;
    private PagedResponse<ContractResponse> pagedResponse;

    @BeforeEach
    void setUp() {
        testContractResponse = ContractResponse.builder()
                .id(1L)
                .contractNumber("CONT-2024-001")
                .contractDocument("BASE16ENCODEDSTRING")
                .contractDocumentName("contract.pdf")
                .contractDocumentContentType("application/pdf")
                .loanPercent(new BigDecimal("75.50"))
                .ltv(new BigDecimal("80.00"))
                .penaltyRatio(new BigDecimal("5.00"))
                .expiryDate(LocalDate.now().plusYears(1))
                .status("ACTIVE")
                .contractTypeId(1L)
                .contractTypeName("TWO_PARTY_WITH_LOGIFIN")
                .partyCount(2)
                .contractManagerId(1L)
                .contractManagerName("John Doe")
                .contractManagerEmail("john.doe@example.com")
                .consignerCompanyId(1L)
                .consignerCompanyName("ABC Company")
                .loanStageId(1L)
                .loanStageName("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1L)
                .build();

        createRequest = CreateContractRequest.builder()
                .contractDocument("BASE16ENCODEDSTRING")
                .contractDocumentName("contract.pdf")
                .contractDocumentContentType("application/pdf")
                .loanPercent(new BigDecimal("75.50"))
                .ltv(new BigDecimal("80.00"))
                .penaltyRatio(new BigDecimal("5.00"))
                .contractNumber("CONT-2024-001")
                .expiryDate(LocalDate.now().plusYears(1))
                .contractTypeId(1L)
                .contractManagerId(1L)
                .consignerCompanyId(1L)
                .loanStageId(1L)
                .status("ACTIVE")
                .build();

        updateRequest = UpdateContractRequest.builder()
                .loanPercent(new BigDecimal("80.00"))
                .ltv(new BigDecimal("85.00"))
                .status("ACTIVE")
                .build();

        pagedResponse = PagedResponse.<ContractResponse>builder()
                .content(Arrays.asList(testContractResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();
    }

    @Nested
    @DisplayName("Create Contract API Tests")
    class CreateContractTests {

        @Test
        @DisplayName("Should create contract successfully")
        @WithMockUser(roles = "TRUST_ACCOUNT")
        void shouldCreateContractSuccessfully() throws Exception {
            when(contractService.createContract(any(CreateContractRequest.class)))
                    .thenReturn(testContractResponse);

            mockMvc.perform(post("/api/v1/contracts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contract created successfully"))
                    .andExpect(jsonPath("$.data.contractNumber").value("CONT-2024-001"))
                    .andExpect(jsonPath("$.data.loanPercent").value(75.50));

            verify(contractService).createContract(any(CreateContractRequest.class));
        }

        @Test
        @DisplayName("Should return 403 when user doesn't have TRUST_ACCOUNT role")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenUserDoesntHaveTrustAccountRole() throws Exception {
            mockMvc.perform(post("/api/v1/contracts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());

            verify(contractService, never()).createContract(any(CreateContractRequest.class));
        }
    }

    @Nested
    @DisplayName("Get Contract API Tests")
    class GetContractTests {

        @Test
        @DisplayName("Should get contract by ID successfully")
        @WithMockUser
        void shouldGetContractByIdSuccessfully() throws Exception {
            when(contractService.getContractById(anyLong())).thenReturn(testContractResponse);

            mockMvc.perform(get("/api/v1/contracts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.contractNumber").value("CONT-2024-001"));

            verify(contractService).getContractById(1L);
        }

        @Test
        @DisplayName("Should get contract by contract number successfully")
        @WithMockUser
        void shouldGetContractByContractNumberSuccessfully() throws Exception {
            when(contractService.getContractByContractNumber(anyString())).thenReturn(testContractResponse);

            mockMvc.perform(get("/api/v1/contracts/by-number/CONT-2024-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contractNumber").value("CONT-2024-001"));

            verify(contractService).getContractByContractNumber("CONT-2024-001");
        }

        @Test
        @DisplayName("Should get all contracts with pagination")
        @WithMockUser
        void shouldGetAllContractsWithPagination() throws Exception {
            when(contractService.getAllContracts(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/contracts")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(contractService).getAllContracts(any());
        }
    }

    @Nested
    @DisplayName("Update Contract API Tests")
    class UpdateContractTests {

        @Test
        @DisplayName("Should update contract successfully")
        @WithMockUser(roles = "TRUST_ACCOUNT")
        void shouldUpdateContractSuccessfully() throws Exception {
            when(contractService.updateContract(anyLong(), any(UpdateContractRequest.class)))
                    .thenReturn(testContractResponse);

            mockMvc.perform(put("/api/v1/contracts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contract updated successfully"));

            verify(contractService).updateContract(anyLong(), any(UpdateContractRequest.class));
        }

        @Test
        @DisplayName("Should return 403 when user doesn't have TRUST_ACCOUNT role for update")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenUserDoesntHaveTrustAccountRoleForUpdate() throws Exception {
            mockMvc.perform(put("/api/v1/contracts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());

            verify(contractService, never()).updateContract(anyLong(), any(UpdateContractRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete Contract API Tests")
    class DeleteContractTests {

        @Test
        @DisplayName("Should delete contract successfully")
        @WithMockUser(roles = "TRUST_ACCOUNT")
        void shouldDeleteContractSuccessfully() throws Exception {
            doNothing().when(contractService).deleteContract(anyLong());

            mockMvc.perform(delete("/api/v1/contracts/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contract deleted successfully"));

            verify(contractService).deleteContract(1L);
        }

        @Test
        @DisplayName("Should return 403 when user doesn't have TRUST_ACCOUNT role for delete")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenUserDoesntHaveTrustAccountRoleForDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/contracts/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(contractService, never()).deleteContract(anyLong());
        }
    }

    @Nested
    @DisplayName("Contract Parties API Tests")
    class ContractPartiesTests {

        @Test
        @DisplayName("Should add party to contract successfully")
        @WithMockUser(roles = "TRUST_ACCOUNT")
        void shouldAddPartyToContractSuccessfully() throws Exception {
            ContractPartyDTO partyDTO = ContractPartyDTO.builder()
                    .userId(2L)
                    .build();
            when(contractService.addPartyToContract(anyLong(), any(ContractPartyDTO.class)))
                    .thenReturn(testContractResponse);

            mockMvc.perform(post("/api/v1/contracts/1/parties")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partyDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Party added successfully"));

            verify(contractService).addPartyToContract(anyLong(), any(ContractPartyDTO.class));
        }

        @Test
        @DisplayName("Should get contract parties successfully")
        @WithMockUser
        void shouldGetContractPartiesSuccessfully() throws Exception {
            List<ContractPartyDTO> parties = Arrays.asList(
                    ContractPartyDTO.builder().id(1L).userId(2L).build()
            );
            when(contractService.getContractParties(anyLong())).thenReturn(parties);

            mockMvc.perform(get("/api/v1/contracts/1/parties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            verify(contractService).getContractParties(1L);
        }
    }

    @Nested
    @DisplayName("Statistics API Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get contract count by type")
        @WithMockUser
        void shouldGetContractCountByType() throws Exception {
            Map<String, Long> stats = new HashMap<>();
            stats.put("SINGLE_PARTY_WITH_LOGIFIN", 5L);
            stats.put("TWO_PARTY_WITH_LOGIFIN", 10L);
            when(contractService.getContractCountByType()).thenReturn(stats);

            mockMvc.perform(get("/api/v1/contracts/stats/count-by-type"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.SINGLE_PARTY_WITH_LOGIFIN").value(5))
                    .andExpect(jsonPath("$.data.TWO_PARTY_WITH_LOGIFIN").value(10));

            verify(contractService).getContractCountByType();
        }

        @Test
        @DisplayName("Should get contract count by status")
        @WithMockUser
        void shouldGetContractCountByStatus() throws Exception {
            Map<String, Long> stats = new HashMap<>();
            stats.put("ACTIVE", 15L);
            stats.put("EXPIRED", 3L);
            when(contractService.getContractCountByStatus()).thenReturn(stats);

            mockMvc.perform(get("/api/v1/contracts/stats/count-by-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.ACTIVE").value(15))
                    .andExpect(jsonPath("$.data.EXPIRED").value(3));

            verify(contractService).getContractCountByStatus();
        }
    }
}
