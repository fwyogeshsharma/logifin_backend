package com.logifin.service;

import com.logifin.dto.DocumentTypeDTO;
import com.logifin.entity.DocumentType;
import com.logifin.exception.BadRequestException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.DocumentTypeRepository;
import com.logifin.repository.TripDocumentRepository;
import com.logifin.service.impl.DocumentTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentTypeService Tests")
class DocumentTypeServiceTest {

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private TripDocumentRepository tripDocumentRepository;

    @InjectMocks
    private DocumentTypeServiceImpl documentTypeService;

    private DocumentType testDocumentType;
    private DocumentType anotherDocumentType;

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
                .code("EWAY_BILL")
                .displayName("E-Way Bill")
                .description("Electronic Way Bill for goods transportation")
                .isActive(true)
                .sortOrder(1)
                .build();
        testDocumentType.setId(1L);
        testDocumentType.setCreatedAt(LocalDateTime.now());
        testDocumentType.setUpdatedAt(LocalDateTime.now());

        anotherDocumentType = DocumentType.builder()
                .code("BILTY")
                .displayName("Bilty/LR")
                .description("Loading Receipt")
                .isActive(true)
                .sortOrder(2)
                .build();
        anotherDocumentType.setId(2L);
        anotherDocumentType.setCreatedAt(LocalDateTime.now());
        anotherDocumentType.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Get All Document Types Tests")
    class GetAllDocumentTypesTests {

        @Test
        @DisplayName("Should get all document types ordered")
        void shouldGetAllDocumentTypesOrdered() {
            when(documentTypeRepository.findAllOrdered())
                    .thenReturn(Arrays.asList(testDocumentType, anotherDocumentType));

            List<DocumentTypeDTO> result = documentTypeService.getAllDocumentTypes();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCode()).isEqualTo("EWAY_BILL");
            assertThat(result.get(1).getCode()).isEqualTo("BILTY");
            verify(documentTypeRepository).findAllOrdered();
        }

        @Test
        @DisplayName("Should return empty list when no document types exist")
        void shouldReturnEmptyListWhenNoDocumentTypesExist() {
            when(documentTypeRepository.findAllOrdered()).thenReturn(Arrays.asList());

            List<DocumentTypeDTO> result = documentTypeService.getAllDocumentTypes();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Active Document Types Tests")
    class GetActiveDocumentTypesTests {

        @Test
        @DisplayName("Should get only active document types")
        void shouldGetOnlyActiveDocumentTypes() {
            when(documentTypeRepository.findByIsActiveTrueOrderBySortOrderAsc())
                    .thenReturn(Arrays.asList(testDocumentType));

            List<DocumentTypeDTO> result = documentTypeService.getActiveDocumentTypes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Get Document Type by ID Tests")
    class GetDocumentTypeByIdTests {

        @Test
        @DisplayName("Should get document type by ID successfully")
        void shouldGetDocumentTypeByIdSuccessfully() {
            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));

            DocumentTypeDTO result = documentTypeService.getDocumentTypeById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCode()).isEqualTo("EWAY_BILL");
            assertThat(result.getDisplayName()).isEqualTo("E-Way Bill");
        }

        @Test
        @DisplayName("Should throw exception when document type not found by ID")
        void shouldThrowExceptionWhenDocumentTypeNotFoundById() {
            when(documentTypeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentTypeService.getDocumentTypeById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DocumentType");
        }
    }

    @Nested
    @DisplayName("Get Document Type by Code Tests")
    class GetDocumentTypeByCodeTests {

        @Test
        @DisplayName("Should get document type by code successfully")
        void shouldGetDocumentTypeByCodeSuccessfully() {
            when(documentTypeRepository.findByCodeIgnoreCase("EWAY_BILL"))
                    .thenReturn(Optional.of(testDocumentType));

            DocumentTypeDTO result = documentTypeService.getDocumentTypeByCode("EWAY_BILL");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("EWAY_BILL");
        }

        @Test
        @DisplayName("Should get document type by code case insensitively")
        void shouldGetDocumentTypeByCodeCaseInsensitively() {
            when(documentTypeRepository.findByCodeIgnoreCase("eway_bill"))
                    .thenReturn(Optional.of(testDocumentType));

            DocumentTypeDTO result = documentTypeService.getDocumentTypeByCode("eway_bill");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("EWAY_BILL");
        }

        @Test
        @DisplayName("Should throw exception when document type not found by code")
        void shouldThrowExceptionWhenDocumentTypeNotFoundByCode() {
            when(documentTypeRepository.findByCodeIgnoreCase("INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentTypeService.getDocumentTypeByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DocumentType");
        }
    }

    @Nested
    @DisplayName("Create Document Type Tests")
    class CreateDocumentTypeTests {

        @Test
        @DisplayName("Should create document type successfully")
        void shouldCreateDocumentTypeSuccessfully() {
            DocumentTypeDTO.CreateRequest request = DocumentTypeDTO.CreateRequest.builder()
                    .code("NEW_DOC")
                    .displayName("New Document")
                    .description("New document type")
                    .sortOrder(5)
                    .build();

            when(documentTypeRepository.existsByCode("NEW_DOC")).thenReturn(false);
            when(documentTypeRepository.save(any(DocumentType.class))).thenReturn(testDocumentType);

            DocumentTypeDTO result = documentTypeService.createDocumentType(request);

            assertThat(result).isNotNull();
            verify(documentTypeRepository).save(any(DocumentType.class));
        }

        @Test
        @DisplayName("Should normalize code to uppercase")
        void shouldNormalizeCodeToUppercase() {
            DocumentTypeDTO.CreateRequest request = DocumentTypeDTO.CreateRequest.builder()
                    .code("new_doc")
                    .displayName("New Document")
                    .build();

            when(documentTypeRepository.existsByCode("NEW_DOC")).thenReturn(false);
            when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
                DocumentType savedType = invocation.getArgument(0);
                assertThat(savedType.getCode()).isEqualTo("NEW_DOC");
                savedType.setId(3L);
                return savedType;
            });

            documentTypeService.createDocumentType(request);

            verify(documentTypeRepository).existsByCode("NEW_DOC");
        }

        @Test
        @DisplayName("Should throw exception when code already exists")
        void shouldThrowExceptionWhenCodeAlreadyExists() {
            DocumentTypeDTO.CreateRequest request = DocumentTypeDTO.CreateRequest.builder()
                    .code("EWAY_BILL")
                    .displayName("E-Way Bill")
                    .build();

            when(documentTypeRepository.existsByCode("EWAY_BILL")).thenReturn(true);

            assertThatThrownBy(() -> documentTypeService.createDocumentType(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("EWAY_BILL")
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should trim whitespace from input fields")
        void shouldTrimWhitespaceFromInputFields() {
            DocumentTypeDTO.CreateRequest request = DocumentTypeDTO.CreateRequest.builder()
                    .code("  NEW_DOC  ")
                    .displayName("  New Document  ")
                    .description("  Description  ")
                    .build();

            when(documentTypeRepository.existsByCode("NEW_DOC")).thenReturn(false);
            when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
                DocumentType savedType = invocation.getArgument(0);
                assertThat(savedType.getCode()).isEqualTo("NEW_DOC");
                assertThat(savedType.getDisplayName()).isEqualTo("New Document");
                assertThat(savedType.getDescription()).isEqualTo("Description");
                savedType.setId(3L);
                return savedType;
            });

            documentTypeService.createDocumentType(request);

            verify(documentTypeRepository).save(any(DocumentType.class));
        }
    }

    @Nested
    @DisplayName("Update Document Type Tests")
    class UpdateDocumentTypeTests {

        @Test
        @DisplayName("Should update document type successfully")
        void shouldUpdateDocumentTypeSuccessfully() {
            DocumentTypeDTO.UpdateRequest request = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated E-Way Bill")
                    .description("Updated description")
                    .isActive(false)
                    .sortOrder(10)
                    .build();

            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(documentTypeRepository.save(any(DocumentType.class))).thenReturn(testDocumentType);

            DocumentTypeDTO result = documentTypeService.updateDocumentType(1L, request);

            assertThat(result).isNotNull();
            verify(documentTypeRepository).save(any(DocumentType.class));
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            DocumentTypeDTO.UpdateRequest request = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated Display Name")
                    .build();

            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
                DocumentType savedType = invocation.getArgument(0);
                assertThat(savedType.getDisplayName()).isEqualTo("Updated Display Name");
                assertThat(savedType.getDescription()).isEqualTo("Electronic Way Bill for goods transportation");
                assertThat(savedType.getIsActive()).isTrue();
                return savedType;
            });

            documentTypeService.updateDocumentType(1L, request);

            verify(documentTypeRepository).save(any(DocumentType.class));
        }

        @Test
        @DisplayName("Should not update display name if empty")
        void shouldNotUpdateDisplayNameIfEmpty() {
            DocumentTypeDTO.UpdateRequest request = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("   ")
                    .sortOrder(5)
                    .build();

            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
                DocumentType savedType = invocation.getArgument(0);
                assertThat(savedType.getDisplayName()).isEqualTo("E-Way Bill");
                assertThat(savedType.getSortOrder()).isEqualTo(5);
                return savedType;
            });

            documentTypeService.updateDocumentType(1L, request);

            verify(documentTypeRepository).save(any(DocumentType.class));
        }

        @Test
        @DisplayName("Should throw exception when document type not found")
        void shouldThrowExceptionWhenDocumentTypeNotFoundForUpdate() {
            DocumentTypeDTO.UpdateRequest request = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated")
                    .build();

            when(documentTypeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentTypeService.updateDocumentType(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DocumentType");
        }
    }

    @Nested
    @DisplayName("Delete Document Type Tests")
    class DeleteDocumentTypeTests {

        @Test
        @DisplayName("Should delete document type successfully when not in use")
        void shouldDeleteDocumentTypeSuccessfullyWhenNotInUse() {
            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(tripDocumentRepository.countByDocumentTypeId(1L)).thenReturn(0L);
            doNothing().when(documentTypeRepository).delete(testDocumentType);

            documentTypeService.deleteDocumentType(1L);

            verify(documentTypeRepository).delete(testDocumentType);
        }

        @Test
        @DisplayName("Should throw exception when document type is in use")
        void shouldThrowExceptionWhenDocumentTypeIsInUse() {
            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(tripDocumentRepository.countByDocumentTypeId(1L)).thenReturn(5L);

            assertThatThrownBy(() -> documentTypeService.deleteDocumentType(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("EWAY_BILL")
                    .hasMessageContaining("5 document(s)");
        }

        @Test
        @DisplayName("Should throw exception when document type not found for delete")
        void shouldThrowExceptionWhenDocumentTypeNotFoundForDelete() {
            when(documentTypeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentTypeService.deleteDocumentType(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DocumentType");
        }
    }

    @Nested
    @DisplayName("Toggle Active Status Tests")
    class ToggleActiveStatusTests {

        @Test
        @DisplayName("Should toggle active to inactive")
        void shouldToggleActiveToInactive() {
            testDocumentType.setIsActive(true);

            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
                DocumentType savedType = invocation.getArgument(0);
                assertThat(savedType.getIsActive()).isFalse();
                return savedType;
            });

            DocumentTypeDTO result = documentTypeService.toggleActiveStatus(1L);

            assertThat(result.getIsActive()).isFalse();
            verify(documentTypeRepository).save(any(DocumentType.class));
        }

        @Test
        @DisplayName("Should toggle inactive to active")
        void shouldToggleInactiveToActive() {
            testDocumentType.setIsActive(false);

            when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
            when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
                DocumentType savedType = invocation.getArgument(0);
                assertThat(savedType.getIsActive()).isTrue();
                return savedType;
            });

            DocumentTypeDTO result = documentTypeService.toggleActiveStatus(1L);

            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when document type not found for toggle")
        void shouldThrowExceptionWhenDocumentTypeNotFoundForToggle() {
            when(documentTypeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentTypeService.toggleActiveStatus(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DocumentType");
        }
    }

    @Nested
    @DisplayName("Code Exists Tests")
    class CodeExistsTests {

        @Test
        @DisplayName("Should return true when code exists")
        void shouldReturnTrueWhenCodeExists() {
            when(documentTypeRepository.existsByCode("EWAY_BILL")).thenReturn(true);

            boolean result = documentTypeService.codeExists("EWAY_BILL");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when code does not exist")
        void shouldReturnFalseWhenCodeDoesNotExist() {
            when(documentTypeRepository.existsByCode("NEW_CODE")).thenReturn(false);

            boolean result = documentTypeService.codeExists("NEW_CODE");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should normalize code before checking")
        void shouldNormalizeCodeBeforeChecking() {
            when(documentTypeRepository.existsByCode("EWAY_BILL")).thenReturn(true);

            boolean result = documentTypeService.codeExists("  eway_bill  ");

            assertThat(result).isTrue();
            verify(documentTypeRepository).existsByCode("EWAY_BILL");
        }
    }
}
