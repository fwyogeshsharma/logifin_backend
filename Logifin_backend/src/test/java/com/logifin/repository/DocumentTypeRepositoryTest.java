package com.logifin.repository;

import com.logifin.entity.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("DocumentTypeRepository Tests")
class DocumentTypeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private DocumentType ewayBillType;
    private DocumentType biltyType;
    private DocumentType podType;
    private DocumentType inactiveType;

    @BeforeEach
    void setUp() {
        // Create test document types
        ewayBillType = DocumentType.builder()
                .code("EWAY_BILL")
                .displayName("E-Way Bill")
                .description("Electronic Way Bill for goods transportation")
                .isActive(true)
                .sortOrder(1)
                .build();
        ewayBillType = entityManager.persistAndFlush(ewayBillType);

        biltyType = DocumentType.builder()
                .code("BILTY")
                .displayName("Bilty")
                .description("Transport receipt / consignment note")
                .isActive(true)
                .sortOrder(2)
                .build();
        biltyType = entityManager.persistAndFlush(biltyType);

        podType = DocumentType.builder()
                .code("POD")
                .displayName("Proof of Delivery")
                .description("Document confirming delivery of goods")
                .isActive(true)
                .sortOrder(3)
                .build();
        podType = entityManager.persistAndFlush(podType);

        inactiveType = DocumentType.builder()
                .code("INACTIVE_TYPE")
                .displayName("Inactive Document Type")
                .description("An inactive document type for testing")
                .isActive(false)
                .sortOrder(99)
                .build();
        inactiveType = entityManager.persistAndFlush(inactiveType);

        entityManager.clear();
    }

    @Nested
    @DisplayName("Find By Code Tests")
    class FindByCodeTests {

        @Test
        @DisplayName("Should find document type by code")
        void findByCode_Success() {
            Optional<DocumentType> result = documentTypeRepository.findByCode("EWAY_BILL");

            assertThat(result).isPresent();
            assertThat(result.get().getDisplayName()).isEqualTo("E-Way Bill");
            assertThat(result.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should return empty when code not found")
        void findByCode_NotFound() {
            Optional<DocumentType> result = documentTypeRepository.findByCode("UNKNOWN_CODE");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find document type by code ignoring case")
        void findByCodeIgnoreCase_Success() {
            Optional<DocumentType> result = documentTypeRepository.findByCodeIgnoreCase("eway_bill");

            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("EWAY_BILL");
        }
    }

    @Nested
    @DisplayName("Exists By Code Tests")
    class ExistsByCodeTests {

        @Test
        @DisplayName("Should return true when code exists")
        void existsByCode_True() {
            boolean exists = documentTypeRepository.existsByCode("BILTY");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when code does not exist")
        void existsByCode_False() {
            boolean exists = documentTypeRepository.existsByCode("NON_EXISTENT");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Find Active Document Types Tests")
    class FindActiveTests {

        @Test
        @DisplayName("Should find only active document types ordered by sort order")
        void findByIsActiveTrue_Success() {
            List<DocumentType> activeTypes = documentTypeRepository.findByIsActiveTrueOrderBySortOrderAsc();

            assertThat(activeTypes).hasSize(3);
            assertThat(activeTypes.get(0).getCode()).isEqualTo("EWAY_BILL");
            assertThat(activeTypes.get(1).getCode()).isEqualTo("BILTY");
            assertThat(activeTypes.get(2).getCode()).isEqualTo("POD");

            // Verify all are active
            assertThat(activeTypes).allMatch(dt -> dt.getIsActive());

            // Verify none is the inactive type
            assertThat(activeTypes).noneMatch(dt -> dt.getCode().equals("INACTIVE_TYPE"));
        }
    }

    @Nested
    @DisplayName("Find All Ordered Tests")
    class FindAllOrderedTests {

        @Test
        @DisplayName("Should find all document types ordered by sort order")
        void findAllOrdered_Success() {
            List<DocumentType> allTypes = documentTypeRepository.findAllOrdered();

            assertThat(allTypes).hasSize(4);
            // Should be ordered by sort_order
            assertThat(allTypes.get(0).getSortOrder()).isEqualTo(1);
            assertThat(allTypes.get(1).getSortOrder()).isEqualTo(2);
            assertThat(allTypes.get(2).getSortOrder()).isEqualTo(3);
            assertThat(allTypes.get(3).getSortOrder()).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudTests {

        @Test
        @DisplayName("Should create new document type")
        void create_Success() {
            DocumentType newType = DocumentType.builder()
                    .code("TRUCK_INVOICE")
                    .displayName("Truck Invoice")
                    .description("Truck billing invoice for the trip")
                    .isActive(true)
                    .sortOrder(4)
                    .build();

            DocumentType saved = documentTypeRepository.save(newType);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCode()).isEqualTo("TRUCK_INVOICE");
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update document type")
        void update_Success() {
            Optional<DocumentType> found = documentTypeRepository.findByCode("POD");
            assertThat(found).isPresent();

            DocumentType toUpdate = found.get();
            toUpdate.setDescription("Updated description");
            toUpdate.setSortOrder(10);

            DocumentType updated = documentTypeRepository.save(toUpdate);

            assertThat(updated.getDescription()).isEqualTo("Updated description");
            assertThat(updated.getSortOrder()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should delete document type")
        void delete_Success() {
            Optional<DocumentType> found = documentTypeRepository.findByCode("INACTIVE_TYPE");
            assertThat(found).isPresent();

            documentTypeRepository.delete(found.get());

            Optional<DocumentType> deleted = documentTypeRepository.findByCode("INACTIVE_TYPE");
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should deactivate document type")
        void deactivate_Success() {
            Optional<DocumentType> found = documentTypeRepository.findByCode("BILTY");
            assertThat(found).isPresent();

            DocumentType toDeactivate = found.get();
            toDeactivate.setIsActive(false);
            documentTypeRepository.save(toDeactivate);

            // Verify it's no longer in active list
            List<DocumentType> activeTypes = documentTypeRepository.findByIsActiveTrueOrderBySortOrderAsc();
            assertThat(activeTypes).noneMatch(dt -> dt.getCode().equals("BILTY"));
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should count all document types")
        void countAll_Success() {
            long count = documentTypeRepository.count();

            assertThat(count).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Predefined Codes Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("Should verify predefined code constants match database")
        void verifyConstants_Success() {
            assertThat(documentTypeRepository.existsByCode(DocumentType.CODE_EWAY_BILL)).isTrue();

            Optional<DocumentType> ewayBill = documentTypeRepository.findByCode(DocumentType.CODE_EWAY_BILL);
            assertThat(ewayBill).isPresent();
            assertThat(ewayBill.get().getDisplayName()).isEqualTo("E-Way Bill");
        }
    }
}
