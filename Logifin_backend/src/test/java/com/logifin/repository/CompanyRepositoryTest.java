package com.logifin.repository;

import com.logifin.entity.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CompanyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company1;
    private Company company2;

    @BeforeEach
    void setUp() {
        company1 = Company.builder()
                .name("Test Company One")
                .displayName("Test Display One")
                .email("test1@company.com")
                .phone("1234567890")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .gstNumber("GST111111111")
                .panNumber("PAN1111111")
                .isActive(true)
                .isVerified(true)
                .build();

        company2 = Company.builder()
                .name("Test Company Two")
                .displayName("Test Display Two")
                .email("test2@company.com")
                .phone("0987654321")
                .city("Delhi")
                .state("Delhi")
                .country("India")
                .gstNumber("GST222222222")
                .panNumber("PAN2222222")
                .isActive(true)
                .isVerified(false)
                .build();

        entityManager.persist(company1);
        entityManager.persist(company2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Find Company by Email - Success")
    void findByEmail_Success() {
        Optional<Company> found = companyRepository.findByEmail("test1@company.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Company One");
    }

    @Test
    @DisplayName("Find Company by Email - Not Found")
    void findByEmail_NotFound() {
        Optional<Company> found = companyRepository.findByEmail("notfound@company.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Find Company by GST Number - Success")
    void findByGstNumber_Success() {
        Optional<Company> found = companyRepository.findByGstNumber("GST111111111");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Company One");
    }

    @Test
    @DisplayName("Find Company by PAN Number - Success")
    void findByPanNumber_Success() {
        Optional<Company> found = companyRepository.findByPanNumber("PAN1111111");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Company One");
    }

    @Test
    @DisplayName("Exists by Email - True")
    void existsByEmail_True() {
        boolean exists = companyRepository.existsByEmail("test1@company.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by Email - False")
    void existsByEmail_False() {
        boolean exists = companyRepository.existsByEmail("notfound@company.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Find Active Companies")
    void findByIsActiveTrue() {
        List<Company> activeCompanies = companyRepository.findByIsActiveTrue();

        assertThat(activeCompanies).hasSize(2);
    }

    @Test
    @DisplayName("Find Verified Companies")
    void findByIsVerifiedTrue() {
        List<Company> verifiedCompanies = companyRepository.findByIsVerifiedTrue();

        assertThat(verifiedCompanies).hasSize(1);
        assertThat(verifiedCompanies.get(0).getName()).isEqualTo("Test Company One");
    }

    @Test
    @DisplayName("Find Unverified Companies")
    void findByIsVerifiedFalse() {
        List<Company> unverifiedCompanies = companyRepository.findByIsVerifiedFalse();

        assertThat(unverifiedCompanies).hasSize(1);
        assertThat(unverifiedCompanies.get(0).getName()).isEqualTo("Test Company Two");
    }

    @Test
    @DisplayName("Search by Keyword - Paginated")
    void searchByKeyword_Paginated() {
        Page<Company> result = companyRepository.searchByKeyword("Test", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Find by City - Paginated")
    void findByCity_Paginated() {
        Page<Company> result = companyRepository.findByCity("Mumbai", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCity()).isEqualTo("Mumbai");
    }

    @Test
    @DisplayName("Find by State - Paginated")
    void findByState_Paginated() {
        Page<Company> result = companyRepository.findByState("Maharashtra", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getState()).isEqualTo("Maharashtra");
    }

    @Test
    @DisplayName("Find Active Companies - Paginated")
    void findByIsActiveTrue_Paginated() {
        Page<Company> result = companyRepository.findByIsActiveTrue(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Find Verified Companies - Paginated")
    void findByIsVerifiedTrue_Paginated() {
        Page<Company> result = companyRepository.findByIsVerifiedTrue(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
