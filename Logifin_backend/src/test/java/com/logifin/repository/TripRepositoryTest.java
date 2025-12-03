package com.logifin.repository;

import com.logifin.entity.Company;
import com.logifin.entity.Role;
import com.logifin.entity.Trip;
import com.logifin.entity.User;
import com.logifin.repository.specification.TripSpecification;
import com.logifin.dto.TripSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TripRepository Tests")
class TripRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Company testCompany;
    private Trip testTrip1;
    private Trip testTrip2;
    private Trip testTrip3;

    @BeforeEach
    void setUp() {
        // Create test role
        Role testRole = Role.builder()
                .roleName("ROLE_TRANSPORTER")
                .description("Transporter Role")
                .build();
        testRole = entityManager.persistAndFlush(testRole);

        // Create test company
        testCompany = Company.builder()
                .name("Test Company")
                .email("company@test.com")
                .phone("1234567890")
                .active(true)
                .build();
        testCompany = entityManager.persistAndFlush(testCompany);

        // Create test user
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .password("password123")
                .phone("9876543210")
                .active(true)
                .company(testCompany)
                .role(testRole)
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        // Create test trips
        testTrip1 = Trip.builder()
                .ewayBillNumber("EWB001")
                .pickup("Mumbai")
                .destination("Delhi")
                .sender("Sender A")
                .receiver("Receiver A")
                .transporter("Fast Logistics")
                .loanAmount(new BigDecimal("100000"))
                .interestRate(new BigDecimal("12.0"))
                .maturityDays(30)
                .distanceKm(new BigDecimal("1400"))
                .loadType("Electronics")
                .weightKg(new BigDecimal("5000"))
                .status(Trip.TripStatus.ACTIVE)
                .createdByUser(testUser)
                .company(testCompany)
                .build();
        testTrip1 = entityManager.persistAndFlush(testTrip1);

        testTrip2 = Trip.builder()
                .ewayBillNumber("EWB002")
                .pickup("Chennai")
                .destination("Bangalore")
                .sender("Sender B")
                .receiver("Receiver B")
                .transporter("Quick Transport")
                .loanAmount(new BigDecimal("200000"))
                .interestRate(new BigDecimal("10.0"))
                .maturityDays(45)
                .distanceKm(new BigDecimal("350"))
                .loadType("Textiles")
                .weightKg(new BigDecimal("3000"))
                .status(Trip.TripStatus.IN_TRANSIT)
                .createdByUser(testUser)
                .company(testCompany)
                .build();
        testTrip2 = entityManager.persistAndFlush(testTrip2);

        testTrip3 = Trip.builder()
                .ewayBillNumber("EWB003")
                .pickup("Mumbai")
                .destination("Pune")
                .sender("Sender C")
                .receiver("Receiver C")
                .transporter("Fast Logistics")
                .loanAmount(new BigDecimal("50000"))
                .interestRate(new BigDecimal("15.0"))
                .maturityDays(15)
                .distanceKm(new BigDecimal("150"))
                .loadType("Food Items")
                .weightKg(new BigDecimal("2000"))
                .status(Trip.TripStatus.COMPLETED)
                .createdByUser(testUser)
                .company(testCompany)
                .build();
        testTrip3 = entityManager.persistAndFlush(testTrip3);

        entityManager.clear();
    }

    @Nested
    @DisplayName("Find By E-way Bill Number Tests")
    class FindByEwayBillNumberTests {

        @Test
        @DisplayName("Should find trip by E-way Bill Number")
        void findByEwayBillNumber_Success() {
            Optional<Trip> result = tripRepository.findByEwayBillNumber("EWB001");

            assertThat(result).isPresent();
            assertThat(result.get().getPickup()).isEqualTo("Mumbai");
            assertThat(result.get().getDestination()).isEqualTo("Delhi");
        }

        @Test
        @DisplayName("Should return empty when E-way Bill Number not found")
        void findByEwayBillNumber_NotFound() {
            Optional<Trip> result = tripRepository.findByEwayBillNumber("EWB999");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if E-way Bill Number exists")
        void existsByEwayBillNumber_True() {
            boolean exists = tripRepository.existsByEwayBillNumber("EWB001");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when E-way Bill Number does not exist")
        void existsByEwayBillNumber_False() {
            boolean exists = tripRepository.existsByEwayBillNumber("EWB999");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Find By Status Tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find trips by status")
        void findByStatus_Success() {
            Page<Trip> result = tripRepository.findByStatus(Trip.TripStatus.ACTIVE, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEwayBillNumber()).isEqualTo("EWB001");
        }

        @Test
        @DisplayName("Should count trips by status")
        void countByStatus_Success() {
            long activeCount = tripRepository.countByStatus(Trip.TripStatus.ACTIVE);
            long inTransitCount = tripRepository.countByStatus(Trip.TripStatus.IN_TRANSIT);
            long completedCount = tripRepository.countByStatus(Trip.TripStatus.COMPLETED);

            assertThat(activeCount).isEqualTo(1);
            assertThat(inTransitCount).isEqualTo(1);
            assertThat(completedCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search trips by keyword")
        void searchByKeyword_Success() {
            Page<Trip> result = tripRepository.searchByKeyword("Mumbai", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2); // testTrip1 and testTrip3 have Mumbai
        }

        @Test
        @DisplayName("Should search trips by transporter")
        void findByTransporter_Success() {
            Page<Trip> result = tripRepository.findByTransporterContainingIgnoreCase("Fast", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get total loan amount")
        void getTotalLoanAmount_Success() {
            BigDecimal total = tripRepository.getTotalLoanAmount();

            assertThat(total).isEqualByComparingTo(new BigDecimal("350000")); // 100000 + 200000 + 50000
        }

        @Test
        @DisplayName("Should get average loan amount")
        void getAverageLoanAmount_Success() {
            BigDecimal average = tripRepository.getAverageLoanAmount();

            assertThat(average).isNotNull();
        }

        @Test
        @DisplayName("Should get average interest rate")
        void getAverageInterestRate_Success() {
            BigDecimal averageRate = tripRepository.getAverageInterestRate();

            assertThat(averageRate).isNotNull();
        }

        @Test
        @DisplayName("Should get total distance")
        void getTotalDistance_Success() {
            BigDecimal totalDistance = tripRepository.getTotalDistance();

            assertThat(totalDistance).isEqualByComparingTo(new BigDecimal("1900")); // 1400 + 350 + 150
        }

        @Test
        @DisplayName("Should get total weight")
        void getTotalWeight_Success() {
            BigDecimal totalWeight = tripRepository.getTotalWeight();

            assertThat(totalWeight).isEqualByComparingTo(new BigDecimal("10000")); // 5000 + 3000 + 2000
        }

        @Test
        @DisplayName("Should get top pickup locations")
        void getTopPickupLocations_Success() {
            List<Object[]> topPickups = tripRepository.getTopPickupLocations(PageRequest.of(0, 10));

            assertThat(topPickups).isNotEmpty();
            // Mumbai appears twice, so it should be first
            assertThat(topPickups.get(0)[0]).isEqualTo("Mumbai");
            assertThat(((Number) topPickups.get(0)[1]).longValue()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should get top transporters")
        void getTopTransporters_Success() {
            List<Object[]> topTransporters = tripRepository.getTopTransporters(PageRequest.of(0, 10));

            assertThat(topTransporters).isNotEmpty();
            assertThat(topTransporters.get(0)[0]).isEqualTo("Fast Logistics");
            assertThat(((Number) topTransporters.get(0)[1]).longValue()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should get trip count by load type")
        void getTripCountByLoadType_Success() {
            List<Object[]> loadTypeCounts = tripRepository.getTripCountByLoadType();

            assertThat(loadTypeCounts).hasSize(3); // Electronics, Textiles, Food Items
        }
    }

    @Nested
    @DisplayName("Bulk Validation Tests")
    class BulkValidationTests {

        @Test
        @DisplayName("Should find existing E-way Bill Numbers")
        void findExistingEwayBillNumbers_Success() {
            List<String> ewayBillNumbers = Arrays.asList("EWB001", "EWB002", "EWB999");

            List<String> existing = tripRepository.findExistingEwayBillNumbers(ewayBillNumbers);

            assertThat(existing).hasSize(2);
            assertThat(existing).contains("EWB001", "EWB002");
            assertThat(existing).doesNotContain("EWB999");
        }
    }

    @Nested
    @DisplayName("Specification Tests")
    class SpecificationTests {

        @Test
        @DisplayName("Should filter by pickup location")
        void filterByPickup_Success() {
            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .pickup("Mumbai")
                    .build();

            Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
            Page<Trip> result = tripRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("Should filter by status")
        void filterByStatus_Success() {
            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .status(Trip.TripStatus.IN_TRANSIT)
                    .build();

            Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
            Page<Trip> result = tripRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEwayBillNumber()).isEqualTo("EWB002");
        }

        @Test
        @DisplayName("Should filter by transporter")
        void filterByTransporter_Success() {
            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .transporter("Fast Logistics")
                    .build();

            Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
            Page<Trip> result = tripRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        void filterByMultipleCriteria_Success() {
            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .pickup("Mumbai")
                    .transporter("Fast Logistics")
                    .status(Trip.TripStatus.ACTIVE)
                    .build();

            Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
            Page<Trip> result = tripRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEwayBillNumber()).isEqualTo("EWB001");
        }

        @Test
        @DisplayName("Should filter by loan amount range")
        void filterByLoanAmountRange_Success() {
            TripSearchCriteria criteria = TripSearchCriteria.builder()
                    .minLoanAmount(new BigDecimal("80000"))
                    .maxLoanAmount(new BigDecimal("150000"))
                    .build();

            Specification<Trip> spec = TripSpecification.fromCriteria(criteria);
            Page<Trip> result = tripRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEwayBillNumber()).isEqualTo("EWB001");
        }
    }

    @Nested
    @DisplayName("Company and User Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should find trips by company ID")
        void findByCompanyId_Success() {
            Page<Trip> result = tripRepository.findByCompanyId(testCompany.getId(), PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("Should find trips by created by user ID")
        void findByCreatedByUserId_Success() {
            Page<Trip> result = tripRepository.findByCreatedByUserId(testUser.getId(), PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(3);
        }
    }
}
