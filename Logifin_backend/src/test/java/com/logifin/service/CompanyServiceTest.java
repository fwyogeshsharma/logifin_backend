package com.logifin.service;

import com.logifin.dto.CompanyDTO;
import com.logifin.entity.Company;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company company;
    private CompanyDTO companyDTO;
    private User verifiedByUser;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .name("Test Company")
                .displayName("Test Display Name")
                .email("test@company.com")
                .phone("1234567890")
                .addressLine1("123 Test Street")
                .city("Test City")
                .state("Test State")
                .pincode("123456")
                .country("India")
                .gstNumber("GST123456789")
                .panNumber("PAN1234567")
                .companyRegistrationNumber("CIN123456")
                .isActive(true)
                .isVerified(false)
                .build();
        company.setId(1L);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        companyDTO = CompanyDTO.builder()
                .name("Test Company")
                .displayName("Test Display Name")
                .email("test@company.com")
                .phone("1234567890")
                .addressLine1("123 Test Street")
                .city("Test City")
                .state("Test State")
                .pincode("123456")
                .country("India")
                .gstNumber("GST123456789")
                .panNumber("PAN1234567")
                .companyRegistrationNumber("CIN123456")
                .build();

        verifiedByUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@test.com")
                .build();
        verifiedByUser.setId(1L);
    }

    @Test
    @DisplayName("Create Company - Success")
    void createCompany_Success() {
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsByGstNumber(anyString())).thenReturn(false);
        when(companyRepository.existsByPanNumber(anyString())).thenReturn(false);
        when(companyRepository.existsByCompanyRegistrationNumber(anyString())).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyDTO result = companyService.createCompany(companyDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(companyDTO.getName());
        assertThat(result.getEmail()).isEqualTo(companyDTO.getEmail());
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Create Company - Duplicate Email")
    void createCompany_DuplicateEmail() {
        when(companyRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(companyDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Create Company - Duplicate GST Number")
    void createCompany_DuplicateGstNumber() {
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsByGstNumber(anyString())).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(companyDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("gstNumber");
    }

    @Test
    @DisplayName("Get Company by ID - Success")
    void getCompanyById_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        CompanyDTO result = companyService.getCompanyById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(company.getName());
    }

    @Test
    @DisplayName("Get Company by ID - Not Found")
    void getCompanyById_NotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company");
    }

    @Test
    @DisplayName("Get Company by Email - Success")
    void getCompanyByEmail_Success() {
        when(companyRepository.findByEmail("test@company.com")).thenReturn(Optional.of(company));

        CompanyDTO result = companyService.getCompanyByEmail("test@company.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@company.com");
    }

    @Test
    @DisplayName("Get All Companies - Success")
    void getAllCompanies_Success() {
        List<Company> companies = Arrays.asList(company);
        when(companyRepository.findAll()).thenReturn(companies);

        List<CompanyDTO> result = companyService.getAllCompanies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(company.getName());
    }

    @Test
    @DisplayName("Get Active Companies - Success")
    void getActiveCompanies_Success() {
        List<Company> companies = Arrays.asList(company);
        when(companyRepository.findByIsActiveTrue()).thenReturn(companies);

        List<CompanyDTO> result = companyService.getActiveCompanies();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Update Company - Success")
    void updateCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        companyDTO.setName("Updated Company Name");
        CompanyDTO result = companyService.updateCompany(1L, companyDTO);

        assertThat(result).isNotNull();
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Update Company - Not Found")
    void updateCompany_NotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.updateCompany(1L, companyDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company");
    }

    @Test
    @DisplayName("Delete Company - Success")
    void deleteCompany_Success() {
        when(companyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(companyRepository).deleteById(1L);

        companyService.deleteCompany(1L);

        verify(companyRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete Company - Not Found")
    void deleteCompany_NotFound() {
        when(companyRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> companyService.deleteCompany(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company");
    }

    @Test
    @DisplayName("Activate Company - Success")
    void activateCompany_Success() {
        company.setIsActive(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        companyService.activateCompany(1L);

        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Deactivate Company - Success")
    void deactivateCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        companyService.deactivateCompany(1L);

        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Verify Company - Success")
    void verifyCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(userRepository.findById(1L)).thenReturn(Optional.of(verifiedByUser));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        companyService.verifyCompany(1L, 1L);

        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Verify Company - Company Not Found")
    void verifyCompany_CompanyNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.verifyCompany(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company");
    }

    @Test
    @DisplayName("Verify Company - User Not Found")
    void verifyCompany_UserNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.verifyCompany(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }
}
