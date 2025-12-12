package com.logifin.service;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.*;
import com.logifin.service.impl.ContractServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractService Tests")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractTypeRepository contractTypeRepository;

    @Mock
    private LoanStageRepository loanStageRepository;

    @Mock
    private ContractPartyRepository contractPartyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Contract testContract;
    private ContractType testContractType;
    private User testUser;
    private Company testCompany;
    private LoanStage testLoanStage;
    private CreateContractRequest createRequest;
    private UpdateContractRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testContractType = ContractType.builder()
                .typeName("TWO_PARTY_WITH_LOGIFIN")
                .description("Two party contract")
                .partyCount(2)
                .build();
        testContractType.setId(1L);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        testUser.setId(1L);

        testCompany = Company.builder()
                .name("ABC Company")
                .email("abc@company.com")
                .gstNumber("GST123456")
                .build();
        testCompany.setId(1L);

        testLoanStage = LoanStage.builder()
                .stageName("PENDING")
                .stageOrder(1)
                .build();
        testLoanStage.setId(1L);

        testContract = Contract.builder()
                .contractDocument("BASE16ENCODEDSTRING")
                .contractDocumentName("contract.pdf")
                .contractDocumentContentType("application/pdf")
                .loanPercent(new BigDecimal("75.50"))
                .ltv(new BigDecimal("80.00"))
                .penaltyRatio(new BigDecimal("5.00"))
                .contractNumber("CONT-2024-001")
                .expiryDate(LocalDate.now().plusYears(1))
                .contractType(testContractType)
                .contractManager(testUser)
                .consignerCompany(testCompany)
                .loanStage(testLoanStage)
                .status("ACTIVE")
                .build();
        testContract.setId(1L);
        testContract.setCreatedAt(LocalDateTime.now());
        testContract.setUpdatedAt(LocalDateTime.now());

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
    }

    @Nested
    @DisplayName("Create Contract Tests")
    class CreateContractTests {

        @Test
        @DisplayName("Should create contract successfully")
        void shouldCreateContractSuccessfully() {
            when(contractRepository.existsByContractNumber(anyString())).thenReturn(false);
            when(contractTypeRepository.findById(anyLong())).thenReturn(Optional.of(testContractType));
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
            when(companyRepository.findById(anyLong())).thenReturn(Optional.of(testCompany));
            when(loanStageRepository.findById(anyLong())).thenReturn(Optional.of(testLoanStage));
            when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

            ContractResponse result = contractService.createContract(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getContractNumber()).isEqualTo("CONT-2024-001");
            assertThat(result.getLoanPercent()).isEqualByComparingTo(new BigDecimal("75.50"));
            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("Should throw exception when contract number already exists")
        void shouldThrowExceptionWhenContractNumberExists() {
            when(contractRepository.existsByContractNumber(anyString())).thenReturn(true);

            assertThatThrownBy(() -> contractService.createContract(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Contract");

            verify(contractRepository, never()).save(any(Contract.class));
        }

        @Test
        @DisplayName("Should throw exception when contract type not found")
        void shouldThrowExceptionWhenContractTypeNotFound() {
            when(contractRepository.existsByContractNumber(anyString())).thenReturn(false);
            when(contractTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.createContract(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ContractType");

            verify(contractRepository, never()).save(any(Contract.class));
        }

        @Test
        @DisplayName("Should throw exception when contract manager not found")
        void shouldThrowExceptionWhenContractManagerNotFound() {
            when(contractRepository.existsByContractNumber(anyString())).thenReturn(false);
            when(contractTypeRepository.findById(anyLong())).thenReturn(Optional.of(testContractType));
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.createContract(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");

            verify(contractRepository, never()).save(any(Contract.class));
        }
    }

    @Nested
    @DisplayName("Get Contract Tests")
    class GetContractTests {

        @Test
        @DisplayName("Should get contract by ID successfully")
        void shouldGetContractByIdSuccessfully() {
            when(contractRepository.findWithDetailsById(anyLong())).thenReturn(Optional.of(testContract));

            ContractResponse result = contractService.getContractById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getContractNumber()).isEqualTo("CONT-2024-001");
            verify(contractRepository).findWithDetailsById(1L);
        }

        @Test
        @DisplayName("Should throw exception when contract not found by ID")
        void shouldThrowExceptionWhenContractNotFoundById() {
            when(contractRepository.findWithDetailsById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getContractById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contract");

            verify(contractRepository).findWithDetailsById(1L);
        }

        @Test
        @DisplayName("Should get contract by contract number successfully")
        void shouldGetContractByContractNumberSuccessfully() {
            when(contractRepository.findByContractNumber(anyString())).thenReturn(Optional.of(testContract));

            ContractResponse result = contractService.getContractByContractNumber("CONT-2024-001");

            assertThat(result).isNotNull();
            assertThat(result.getContractNumber()).isEqualTo("CONT-2024-001");
            verify(contractRepository).findByContractNumber("CONT-2024-001");
        }

        @Test
        @DisplayName("Should get all contracts with pagination")
        void shouldGetAllContractsWithPagination() {
            List<Contract> contracts = Arrays.asList(testContract);
            Page<Contract> contractPage = new PageImpl<>(contracts);
            when(contractRepository.findAllWithDetails(any(Pageable.class))).thenReturn(contractPage);

            PagedResponse<ContractResponse> result = contractService.getAllContracts(PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getContractNumber()).isEqualTo("CONT-2024-001");
            verify(contractRepository).findAllWithDetails(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Update Contract Tests")
    class UpdateContractTests {

        @Test
        @DisplayName("Should update contract successfully")
        void shouldUpdateContractSuccessfully() {
            when(contractRepository.findById(anyLong())).thenReturn(Optional.of(testContract));
            when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

            ContractResponse result = contractService.updateContract(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent contract")
        void shouldThrowExceptionWhenUpdatingNonExistentContract() {
            when(contractRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.updateContract(1L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contract");

            verify(contractRepository, never()).save(any(Contract.class));
        }
    }

    @Nested
    @DisplayName("Delete Contract Tests")
    class DeleteContractTests {

        @Test
        @DisplayName("Should delete contract successfully")
        void shouldDeleteContractSuccessfully() {
            when(contractRepository.existsById(anyLong())).thenReturn(true);
            doNothing().when(contractRepository).deleteById(anyLong());

            contractService.deleteContract(1L);

            verify(contractRepository).existsById(1L);
            verify(contractRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent contract")
        void shouldThrowExceptionWhenDeletingNonExistentContract() {
            when(contractRepository.existsById(anyLong())).thenReturn(false);

            assertThatThrownBy(() -> contractService.deleteContract(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contract");

            verify(contractRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get contract count by status")
        void shouldGetContractCountByStatus() {
            when(contractRepository.countByStatus(anyString())).thenReturn(5L);

            long count = contractService.countContractsByStatus("ACTIVE");

            assertThat(count).isEqualTo(5L);
            verify(contractRepository).countByStatus("ACTIVE");
        }

        @Test
        @DisplayName("Should get average loan percent")
        void shouldGetAverageLoanPercent() {
            when(contractRepository.getAverageLoanPercent()).thenReturn(new BigDecimal("75.00"));

            BigDecimal average = contractService.getAverageLoanPercent();

            assertThat(average).isEqualByComparingTo(new BigDecimal("75.00"));
            verify(contractRepository).getAverageLoanPercent();
        }

        @Test
        @DisplayName("Should get contract count by type")
        void shouldGetContractCountByType() {
            List<Object[]> mockResults = Arrays.asList(
                    new Object[]{"SINGLE_PARTY_WITH_LOGIFIN", 5L},
                    new Object[]{"TWO_PARTY_WITH_LOGIFIN", 10L}
            );
            when(contractRepository.getContractCountByType()).thenReturn(mockResults);

            Map<String, Long> result = contractService.getContractCountByType();

            assertThat(result).hasSize(2);
            assertThat(result.get("SINGLE_PARTY_WITH_LOGIFIN")).isEqualTo(5L);
            assertThat(result.get("TWO_PARTY_WITH_LOGIFIN")).isEqualTo(10L);
            verify(contractRepository).getContractCountByType();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should check if contract is expired")
        void shouldCheckIfContractIsExpired() {
            Contract expiredContract = Contract.builder()
                    .expiryDate(LocalDate.now().minusDays(1))
                    .build();
            expiredContract.setId(1L);
            when(contractRepository.findById(anyLong())).thenReturn(Optional.of(expiredContract));

            boolean isExpired = contractService.isContractExpired(1L);

            assertThat(isExpired).isTrue();
            verify(contractRepository).findById(1L);
        }

        @Test
        @DisplayName("Should check if user is party to contract")
        void shouldCheckIfUserIsPartyToContract() {
            when(contractPartyRepository.existsByContractIdAndUserId(anyLong(), anyLong())).thenReturn(true);

            boolean isParty = contractService.isUserPartyToContract(1L, 2L);

            assertThat(isParty).isTrue();
            verify(contractPartyRepository).existsByContractIdAndUserId(1L, 2L);
        }
    }
}
