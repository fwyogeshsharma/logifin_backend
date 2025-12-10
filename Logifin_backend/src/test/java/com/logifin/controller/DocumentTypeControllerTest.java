package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.DocumentTypeDTO;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.BadRequestException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.DocumentTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("DocumentTypeController Tests")
class DocumentTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockBean
    private DocumentTypeService documentTypeService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    private DocumentTypeDTO testDocumentTypeDTO;
    private String superAdminToken;
    private String adminToken;
    private String csrToken;

    @BeforeEach
    void setUp() {
        testDocumentTypeDTO = DocumentTypeDTO.builder()
                .id(1L)
                .code("EWAY_BILL")
                .displayName("E-Way Bill")
                .description("Electronic Way Bill for goods transportation")
                .isActive(true)
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        superAdminToken = createTokenForRole("ROLE_SUPER_ADMIN", 1L, "superadmin@test.com");
        adminToken = createTokenForRole("ROLE_ADMIN", 2L, "admin@test.com");
        csrToken = createTokenForRole("ROLE_CSR", 3L, "csr@test.com");
    }

    private String createTokenForRole(String roleName, Long userId, String email) {
        Role role = Role.builder().roleName(roleName).build();
        role.setId(1L);
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("password")
                .active(true)
                .role(role)
                .build();
        user.setId(userId);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, Collections.singletonList(new SimpleGrantedAuthority(roleName)));
        return tokenProvider.generateToken(authentication);
    }

    @Nested
    @DisplayName("GET /api/v1/document-types Tests")
    class GetAllDocumentTypesTests {

        @Test
        @DisplayName("Should get all document types with authentication")
        void shouldGetAllDocumentTypesWithAuth() throws Exception {
            when(documentTypeService.getAllDocumentTypes()).thenReturn(Arrays.asList(testDocumentTypeDTO));

            mockMvc.perform(get("/api/v1/document-types")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].code").value("EWAY_BILL"))
                    .andExpect(jsonPath("$.data[0].displayName").value("E-Way Bill"));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturnUnauthorizedWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/document-types"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/document-types/active Tests")
    class GetActiveDocumentTypesTests {

        @Test
        @DisplayName("Should get active document types")
        void shouldGetActiveDocumentTypes() throws Exception {
            when(documentTypeService.getActiveDocumentTypes()).thenReturn(Arrays.asList(testDocumentTypeDTO));

            mockMvc.perform(get("/api/v1/document-types/active")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].isActive").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/document-types/{id} Tests")
    class GetDocumentTypeByIdTests {

        @Test
        @DisplayName("Should get document type by ID")
        void shouldGetDocumentTypeById() throws Exception {
            when(documentTypeService.getDocumentTypeById(1L)).thenReturn(testDocumentTypeDTO);

            mockMvc.perform(get("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.code").value("EWAY_BILL"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent document type")
        void shouldReturn404ForNonExistentDocumentType() throws Exception {
            when(documentTypeService.getDocumentTypeById(999L))
                    .thenThrow(new ResourceNotFoundException("DocumentType", "id", 999L));

            mockMvc.perform(get("/api/v1/document-types/999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/document-types/code/{code} Tests")
    class GetDocumentTypeByCodeTests {

        @Test
        @DisplayName("Should get document type by code")
        void shouldGetDocumentTypeByCode() throws Exception {
            when(documentTypeService.getDocumentTypeByCode("EWAY_BILL")).thenReturn(testDocumentTypeDTO);

            mockMvc.perform(get("/api/v1/document-types/code/EWAY_BILL")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("EWAY_BILL"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent code")
        void shouldReturn404ForNonExistentCode() throws Exception {
            when(documentTypeService.getDocumentTypeByCode("INVALID"))
                    .thenThrow(new ResourceNotFoundException("DocumentType", "code", "INVALID"));

            mockMvc.perform(get("/api/v1/document-types/code/INVALID")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/document-types/exists/{code} Tests")
    class CheckCodeExistsTests {

        @Test
        @DisplayName("Should return true if code exists")
        void shouldReturnTrueIfCodeExists() throws Exception {
            when(documentTypeService.codeExists("EWAY_BILL")).thenReturn(true);

            mockMvc.perform(get("/api/v1/document-types/exists/EWAY_BILL")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("Should return false if code does not exist")
        void shouldReturnFalseIfCodeDoesNotExist() throws Exception {
            when(documentTypeService.codeExists("NEW_CODE")).thenReturn(false);

            mockMvc.perform(get("/api/v1/document-types/exists/NEW_CODE")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/document-types Tests")
    class CreateDocumentTypeTests {

        @Test
        @DisplayName("Should create document type with Super Admin role")
        void shouldCreateDocumentTypeWithSuperAdminRole() throws Exception {
            DocumentTypeDTO.CreateRequest createRequest = DocumentTypeDTO.CreateRequest.builder()
                    .code("NEW_DOC")
                    .displayName("New Document")
                    .description("New document type")
                    .sortOrder(5)
                    .build();

            when(documentTypeService.createDocumentType(any(DocumentTypeDTO.CreateRequest.class)))
                    .thenReturn(testDocumentTypeDTO);

            mockMvc.perform(post("/api/v1/document-types")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Document type created successfully"));
        }

        @Test
        @DisplayName("Should create document type with Admin role")
        void shouldCreateDocumentTypeWithAdminRole() throws Exception {
            DocumentTypeDTO.CreateRequest createRequest = DocumentTypeDTO.CreateRequest.builder()
                    .code("NEW_DOC")
                    .displayName("New Document")
                    .description("New document type")
                    .sortOrder(5)
                    .build();

            when(documentTypeService.createDocumentType(any(DocumentTypeDTO.CreateRequest.class)))
                    .thenReturn(testDocumentTypeDTO);

            mockMvc.perform(post("/api/v1/document-types")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to create")
        void shouldReturn403ForCsrRoleCreate() throws Exception {
            DocumentTypeDTO.CreateRequest createRequest = DocumentTypeDTO.CreateRequest.builder()
                    .code("NEW_DOC")
                    .displayName("New Document")
                    .build();

            mockMvc.perform(post("/api/v1/document-types")
                            .header("Authorization", "Bearer " + csrToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 for duplicate code")
        void shouldReturn400ForDuplicateCode() throws Exception {
            DocumentTypeDTO.CreateRequest createRequest = DocumentTypeDTO.CreateRequest.builder()
                    .code("EWAY_BILL")
                    .displayName("E-Way Bill")
                    .build();

            when(documentTypeService.createDocumentType(any(DocumentTypeDTO.CreateRequest.class)))
                    .thenThrow(new BadRequestException("Document type with code 'EWAY_BILL' already exists"));

            mockMvc.perform(post("/api/v1/document-types")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() throws Exception {
            DocumentTypeDTO.CreateRequest createRequest = DocumentTypeDTO.CreateRequest.builder()
                    .code("")
                    .displayName("")
                    .build();

            mockMvc.perform(post("/api/v1/document-types")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/document-types/{id} Tests")
    class UpdateDocumentTypeTests {

        @Test
        @DisplayName("Should update document type with Super Admin role")
        void shouldUpdateDocumentTypeWithSuperAdminRole() throws Exception {
            DocumentTypeDTO.UpdateRequest updateRequest = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated E-Way Bill")
                    .description("Updated description")
                    .isActive(true)
                    .sortOrder(2)
                    .build();

            when(documentTypeService.updateDocumentType(anyLong(), any(DocumentTypeDTO.UpdateRequest.class)))
                    .thenReturn(testDocumentTypeDTO);

            mockMvc.perform(put("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Document type updated successfully"));
        }

        @Test
        @DisplayName("Should update document type with Admin role")
        void shouldUpdateDocumentTypeWithAdminRole() throws Exception {
            DocumentTypeDTO.UpdateRequest updateRequest = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated E-Way Bill")
                    .build();

            when(documentTypeService.updateDocumentType(anyLong(), any(DocumentTypeDTO.UpdateRequest.class)))
                    .thenReturn(testDocumentTypeDTO);

            mockMvc.perform(put("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to update")
        void shouldReturn403ForCsrRoleUpdate() throws Exception {
            DocumentTypeDTO.UpdateRequest updateRequest = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated E-Way Bill")
                    .build();

            mockMvc.perform(put("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + csrToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent document type")
        void shouldReturn404ForNonExistentDocumentType() throws Exception {
            DocumentTypeDTO.UpdateRequest updateRequest = DocumentTypeDTO.UpdateRequest.builder()
                    .displayName("Updated")
                    .build();

            when(documentTypeService.updateDocumentType(eq(999L), any(DocumentTypeDTO.UpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("DocumentType", "id", 999L));

            mockMvc.perform(put("/api/v1/document-types/999")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/document-types/{id} Tests")
    class DeleteDocumentTypeTests {

        @Test
        @DisplayName("Should delete document type with Super Admin role")
        void shouldDeleteDocumentTypeWithSuperAdminRole() throws Exception {
            doNothing().when(documentTypeService).deleteDocumentType(1L);

            mockMvc.perform(delete("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Document type deleted successfully"));
        }

        @Test
        @DisplayName("Should return 403 for Admin role trying to delete")
        void shouldReturn403ForAdminRoleDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to delete")
        void shouldReturn403ForCsrRoleDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent document type")
        void shouldReturn404ForNonExistentDocumentType() throws Exception {
            doThrow(new ResourceNotFoundException("DocumentType", "id", 999L))
                    .when(documentTypeService).deleteDocumentType(999L);

            mockMvc.perform(delete("/api/v1/document-types/999")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when document type is in use")
        void shouldReturn400WhenDocumentTypeInUse() throws Exception {
            doThrow(new BadRequestException("Cannot delete document type 'EWAY_BILL' as it is being used by 5 document(s)"))
                    .when(documentTypeService).deleteDocumentType(1L);

            mockMvc.perform(delete("/api/v1/document-types/1")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/document-types/{id}/toggle-status Tests")
    class ToggleStatusTests {

        @Test
        @DisplayName("Should toggle status with Super Admin role")
        void shouldToggleStatusWithSuperAdminRole() throws Exception {
            DocumentTypeDTO toggledDTO = DocumentTypeDTO.builder()
                    .id(1L)
                    .code("EWAY_BILL")
                    .displayName("E-Way Bill")
                    .isActive(false)
                    .build();

            when(documentTypeService.toggleActiveStatus(1L)).thenReturn(toggledDTO);

            mockMvc.perform(patch("/api/v1/document-types/1/toggle-status")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Document type deactivated successfully"))
                    .andExpect(jsonPath("$.data.isActive").value(false));
        }

        @Test
        @DisplayName("Should toggle status with Admin role")
        void shouldToggleStatusWithAdminRole() throws Exception {
            DocumentTypeDTO toggledDTO = DocumentTypeDTO.builder()
                    .id(1L)
                    .code("EWAY_BILL")
                    .displayName("E-Way Bill")
                    .isActive(true)
                    .build();

            when(documentTypeService.toggleActiveStatus(1L)).thenReturn(toggledDTO);

            mockMvc.perform(patch("/api/v1/document-types/1/toggle-status")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Document type activated successfully"));
        }

        @Test
        @DisplayName("Should return 403 for CSR role trying to toggle status")
        void shouldReturn403ForCsrRoleToggle() throws Exception {
            mockMvc.perform(patch("/api/v1/document-types/1/toggle-status")
                            .header("Authorization", "Bearer " + csrToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent document type")
        void shouldReturn404ForNonExistentDocumentType() throws Exception {
            when(documentTypeService.toggleActiveStatus(999L))
                    .thenThrow(new ResourceNotFoundException("DocumentType", "id", 999L));

            mockMvc.perform(patch("/api/v1/document-types/999/toggle-status")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
