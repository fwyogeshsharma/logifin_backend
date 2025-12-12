package com.logifin.repository;

import com.logifin.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ContractRepository Tests")
class ContractRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractTypeRepository contractTypeRepository;

    @Autowired
    private LoanStageRepository loanStageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Contract testContract;
    private ContractType testContractType;
    private User testUser;
    private Company testCompany;
    private LoanStage testLoanStage;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Create dependencies
        testRole = Role.builder()
                .roleName("ROLE_TRUST_ACCOUNT")
                .description("Trust Account Role")
                .build();
        entityManager.persist(testRole);

        testContractType = ContractType.builder()
                .typeName("TWO_PARTY_WITH_LOGIFIN")
                .description("Two party contract")
                .partyCount(2)
                .build();
        entityManager.persist(testContractType);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("password")
                .active(true)
                .role(testRole)
                .build();
        entityManager.persist(testUser);

        testCompany = Company.builder()
                .name("ABC Company")
                .displayName("ABC Co.")
                .email("abc@company.com")
                .gstNumber("GST123456")
                .panNumber("PAN123456")
                .isActive(true)
                .isVerified(true)
                .build();
        entityManager.persist(testCompany);

        testLoanStage = LoanStage.builder()
                .stageName("PENDING")
                .description("Initial stage")
                .stageOrder(1)
                .build();
        entityManager.persist(testLoanStage);

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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save contract successfully")
    void shouldSaveContractSuccessfully() {
        Contract savedContract = contractRepository.save(testContract);

        assertThat(savedContract).isNotNull();
        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getContractNumber()).isEqualTo("CONT-2024-001");
        assertThat(savedContract.getLoanPercent()).isEqualByComparingTo(new BigDecimal("75.50"));
    }

    @Test
    @DisplayName("Should find contract by ID")
    void shouldFindContractById() {
        Contract savedContract = contractRepository.save(testContract);
        entityManager.flush();
        entityManager.clear();

        Optional<Contract> foundContract = contractRepository.findById(savedContract.getId());

        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getContractNumber()).isEqualTo("CONT-2024-001");
    }

    @Test
    @DisplayName("Should find contract by contract number")
    void shouldFindContractByContractNumber() {
        contractRepository.save(testContract);
        entityManager.flush();
        entityManager.clear();

        Optional<Contract> foundContract = contractRepository.findByContractNumber("CONT-2024-001");

        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getContractNumber()).isEqualTo("CONT-2024-001");
    }

    @Test
    @DisplayName("Should check if contract number exists")
    void shouldCheckIfContractNumberExists() {
        contractRepository.save(testContract);
        entityManager.flush();

        boolean exists = contractRepository.existsByContractNumber("CONT-2024-001");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find contracts by status")
    void shouldFindContractsByStatus() {
        contractRepository.save(testContract);
        entityManager.flush();

        Page<Contract> contracts = contractRepository.findByStatus("ACTIVE", PageRequest.of(0, 10));

        assertThat(contracts).isNotEmpty();
        assertThat(contracts.getContent().get(0).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should find contracts by contract type")
    void shouldFindContractsByContractType() {
        contractRepository.save(testContract);
        entityManager.flush();

        Page<Contract> contracts = contractRepository.findByContractTypeId(
                testContractType.getId(), PageRequest.of(0, 10));

        assertThat(contracts).isNotEmpty();
        assertThat(contracts.getContent().get(0).getContractType().getId()).isEqualTo(testContractType.getId());
    }

    @Test
    @DisplayName("Should find contracts by manager")
    void shouldFindContractsByManager() {
        contractRepository.save(testContract);
        entityManager.flush();

        Page<Contract> contracts = contractRepository.findByContractManagerId(
                testUser.getId(), PageRequest.of(0, 10));

        assertThat(contracts).isNotEmpty();
        assertThat(contracts.getContent().get(0).getContractManager().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should find contracts by company")
    void shouldFindContractsByCompany() {
        contractRepository.save(testContract);
        entityManager.flush();

        Page<Contract> contracts = contractRepository.findByConsignerCompanyId(
                testCompany.getId(), PageRequest.of(0, 10));

        assertThat(contracts).isNotEmpty();
        assertThat(contracts.getContent().get(0).getConsignerCompany().getId()).isEqualTo(testCompany.getId());
    }

    @Test
    @DisplayName("Should count contracts by status")
    void shouldCountContractsByStatus() {
        contractRepository.save(testContract);
        entityManager.flush();

        long count = contractRepository.countByStatus("ACTIVE");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should get average loan percent")
    void shouldGetAverageLoanPercent() {
        contractRepository.save(testContract);
        entityManager.flush();

        BigDecimal average = contractRepository.getAverageLoanPercent();

        assertThat(average).isNotNull();
        assertThat(average).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should search contracts by keyword")
    void shouldSearchContractsByKeyword() {
        contractRepository.save(testContract);
        entityManager.flush();

        Page<Contract> contracts = contractRepository.searchByKeyword("CONT", PageRequest.of(0, 10));

        assertThat(contracts).isNotEmpty();
        assertThat(contracts.getContent().get(0).getContractNumber()).contains("CONT");
    }

    @Test
    @DisplayName("Should find expired contracts")
    void shouldFindExpiredContracts() {
        Contract expiredContract = Contract.builder()
                .contractDocument("BASE16ENCODEDSTRING")
                .contractDocumentName("expired_contract.pdf")
                .contractDocumentContentType("application/pdf")
                .loanPercent(new BigDecimal("75.50"))
                .ltv(new BigDecimal("80.00"))
                .penaltyRatio(new BigDecimal("5.00"))
                .contractNumber("CONT-2024-002")
                .expiryDate(LocalDate.now().minusDays(1))
                .contractType(testContractType)
                .contractManager(testUser)
                .consignerCompany(testCompany)
                .loanStage(testLoanStage)
                .status("ACTIVE")
                .build();
        contractRepository.save(expiredContract);
        entityManager.flush();

        List<Contract> expiredContracts = contractRepository.findExpiredContracts();

        assertThat(expiredContracts).isNotEmpty();
        assertThat(expiredContracts.get(0).getExpiryDate()).isBefore(LocalDate.now());
    }

    @Test
    @DisplayName("Should delete contract successfully")
    void shouldDeleteContractSuccessfully() {
        Contract savedContract = contractRepository.save(testContract);
        entityManager.flush();
        Long contractId = savedContract.getId();

        contractRepository.deleteById(contractId);
        entityManager.flush();

        Optional<Contract> deletedContract = contractRepository.findById(contractId);
        assertThat(deletedContract).isEmpty();
    }
}
