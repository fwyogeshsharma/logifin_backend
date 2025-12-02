package com.logifin.service.impl;

import com.logifin.config.CacheConfig;
import com.logifin.dto.CompanyDTO;
import com.logifin.dto.PagedResponse;
import com.logifin.entity.Company;
import com.logifin.entity.User;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    @CacheEvict(value = CacheConfig.CACHE_COMPANIES, allEntries = true)
    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        log.debug("Creating company: {}", companyDTO.getName());

        // Check for duplicate email
        if (companyDTO.getEmail() != null && companyRepository.existsByEmail(companyDTO.getEmail())) {
            throw new DuplicateResourceException("Company", "email", companyDTO.getEmail());
        }

        // Check for duplicate GST number
        if (companyDTO.getGstNumber() != null && companyRepository.existsByGstNumber(companyDTO.getGstNumber())) {
            throw new DuplicateResourceException("Company", "gstNumber", companyDTO.getGstNumber());
        }

        // Check for duplicate PAN number
        if (companyDTO.getPanNumber() != null && companyRepository.existsByPanNumber(companyDTO.getPanNumber())) {
            throw new DuplicateResourceException("Company", "panNumber", companyDTO.getPanNumber());
        }

        // Check for duplicate registration number
        if (companyDTO.getCompanyRegistrationNumber() != null &&
            companyRepository.existsByCompanyRegistrationNumber(companyDTO.getCompanyRegistrationNumber())) {
            throw new DuplicateResourceException("Company", "companyRegistrationNumber", companyDTO.getCompanyRegistrationNumber());
        }

        Company company = mapToEntity(companyDTO);
        Company savedCompany = companyRepository.save(company);
        return mapToDTO(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_COMPANY_BY_ID, key = "#id", unless = "#result == null")
    public CompanyDTO getCompanyById(Long id) {
        log.debug("Fetching company by id: {} from database", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        return mapToDTO(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyByEmail(String email) {
        log.debug("Fetching company by email: {} from database", email);
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "email", email));
        return mapToDTO(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyByGstNumber(String gstNumber) {
        log.debug("Fetching company by GST number: {} from database", gstNumber);
        Company company = companyRepository.findByGstNumber(gstNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "gstNumber", gstNumber));
        return mapToDTO(company);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_COMPANIES, unless = "#result == null || #result.isEmpty()")
    public List<CompanyDTO> getAllCompanies() {
        log.debug("Fetching all companies from database");
        return companyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> getActiveCompanies() {
        log.debug("Fetching active companies from database");
        return companyRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> getVerifiedCompanies() {
        log.debug("Fetching verified companies from database");
        return companyRepository.findByIsVerifiedTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> getPendingVerificationCompanies() {
        log.debug("Fetching pending verification companies from database");
        return companyRepository.findByIsVerifiedFalse().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_COMPANY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_COMPANIES, allEntries = true)
    })
    public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO) {
        log.debug("Updating company with id: {}", id);
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        // Check for duplicate email (if changed)
        if (companyDTO.getEmail() != null && !companyDTO.getEmail().equals(existingCompany.getEmail())
                && companyRepository.existsByEmail(companyDTO.getEmail())) {
            throw new DuplicateResourceException("Company", "email", companyDTO.getEmail());
        }

        // Check for duplicate GST number (if changed)
        if (companyDTO.getGstNumber() != null && !companyDTO.getGstNumber().equals(existingCompany.getGstNumber())
                && companyRepository.existsByGstNumber(companyDTO.getGstNumber())) {
            throw new DuplicateResourceException("Company", "gstNumber", companyDTO.getGstNumber());
        }

        // Check for duplicate PAN number (if changed)
        if (companyDTO.getPanNumber() != null && !companyDTO.getPanNumber().equals(existingCompany.getPanNumber())
                && companyRepository.existsByPanNumber(companyDTO.getPanNumber())) {
            throw new DuplicateResourceException("Company", "panNumber", companyDTO.getPanNumber());
        }

        // Check for duplicate registration number (if changed)
        if (companyDTO.getCompanyRegistrationNumber() != null
                && !companyDTO.getCompanyRegistrationNumber().equals(existingCompany.getCompanyRegistrationNumber())
                && companyRepository.existsByCompanyRegistrationNumber(companyDTO.getCompanyRegistrationNumber())) {
            throw new DuplicateResourceException("Company", "companyRegistrationNumber", companyDTO.getCompanyRegistrationNumber());
        }

        // Update fields
        existingCompany.setName(companyDTO.getName());
        existingCompany.setDisplayName(companyDTO.getDisplayName());
        existingCompany.setLogoBase64(companyDTO.getLogoBase64());
        existingCompany.setDescription(companyDTO.getDescription());
        existingCompany.setWebsite(companyDTO.getWebsite());
        existingCompany.setEmail(companyDTO.getEmail());
        existingCompany.setPhone(companyDTO.getPhone());
        existingCompany.setAddressLine1(companyDTO.getAddressLine1());
        existingCompany.setAddressLine2(companyDTO.getAddressLine2());
        existingCompany.setCity(companyDTO.getCity());
        existingCompany.setState(companyDTO.getState());
        existingCompany.setPincode(companyDTO.getPincode());
        existingCompany.setCountry(companyDTO.getCountry());
        existingCompany.setGstNumber(companyDTO.getGstNumber());
        existingCompany.setPanNumber(companyDTO.getPanNumber());
        existingCompany.setCompanyRegistrationNumber(companyDTO.getCompanyRegistrationNumber());

        if (companyDTO.getIsActive() != null) {
            existingCompany.setIsActive(companyDTO.getIsActive());
        }

        Company updatedCompany = companyRepository.save(existingCompany);
        return mapToDTO(updatedCompany);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_COMPANY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_COMPANIES, allEntries = true)
    })
    public void deleteCompany(Long id) {
        log.debug("Deleting company with id: {}", id);
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company", "id", id);
        }
        companyRepository.deleteById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_COMPANY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_COMPANIES, allEntries = true)
    })
    public void activateCompany(Long id) {
        log.debug("Activating company with id: {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        company.setIsActive(true);
        companyRepository.save(company);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_COMPANY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_COMPANIES, allEntries = true)
    })
    public void deactivateCompany(Long id) {
        log.debug("Deactivating company with id: {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        company.setIsActive(false);
        companyRepository.save(company);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_COMPANY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_COMPANIES, allEntries = true)
    })
    public void verifyCompany(Long id, Long verifiedByUserId) {
        log.debug("Verifying company with id: {} by user: {}", id, verifiedByUserId);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        User verifiedBy = userRepository.findById(verifiedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", verifiedByUserId));

        company.setIsVerified(true);
        company.setVerifiedAt(LocalDateTime.now());
        company.setVerifiedBy(verifiedBy);
        companyRepository.save(company);
    }

    // Paginated methods

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getAllCompanies(Pageable pageable) {
        log.debug("Fetching all companies with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findAll(pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getActiveCompanies(Pageable pageable) {
        log.debug("Fetching active companies with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findByIsActiveTrue(pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getInactiveCompanies(Pageable pageable) {
        log.debug("Fetching inactive companies with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findByIsActiveFalse(pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getVerifiedCompanies(Pageable pageable) {
        log.debug("Fetching verified companies with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findByIsVerifiedTrue(pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getPendingVerificationCompanies(Pageable pageable) {
        log.debug("Fetching pending verification companies with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findByIsVerifiedFalse(pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> searchCompanies(String keyword, Pageable pageable) {
        log.debug("Searching companies by keyword: {} with pagination: page={}, size={}", keyword, pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.searchByKeyword(keyword, pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getCompaniesByCity(String city, Pageable pageable) {
        log.debug("Fetching companies by city: {} with pagination: page={}, size={}", city, pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findByCity(city, pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getCompaniesByState(String state, Pageable pageable) {
        log.debug("Fetching companies by state: {} with pagination: page={}, size={}", state, pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companyPage = companyRepository.findByState(state, pageable);
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(companyPage, companyDTOs);
    }

    private CompanyDTO mapToDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .displayName(company.getDisplayName())
                .logoBase64(company.getLogoBase64())
                .description(company.getDescription())
                .website(company.getWebsite())
                .email(company.getEmail())
                .phone(company.getPhone())
                .addressLine1(company.getAddressLine1())
                .addressLine2(company.getAddressLine2())
                .city(company.getCity())
                .state(company.getState())
                .pincode(company.getPincode())
                .country(company.getCountry())
                .gstNumber(company.getGstNumber())
                .panNumber(company.getPanNumber())
                .companyRegistrationNumber(company.getCompanyRegistrationNumber())
                .isActive(company.getIsActive())
                .isVerified(company.getIsVerified())
                .verifiedAt(company.getVerifiedAt())
                .verifiedById(company.getVerifiedBy() != null ? company.getVerifiedBy().getId() : null)
                .verifiedByName(company.getVerifiedBy() != null ?
                        company.getVerifiedBy().getFirstName() + " " + company.getVerifiedBy().getLastName() : null)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private Company mapToEntity(CompanyDTO dto) {
        return Company.builder()
                .name(dto.getName())
                .displayName(dto.getDisplayName())
                .logoBase64(dto.getLogoBase64())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .country(dto.getCountry() != null ? dto.getCountry() : "India")
                .gstNumber(dto.getGstNumber())
                .panNumber(dto.getPanNumber())
                .companyRegistrationNumber(dto.getCompanyRegistrationNumber())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .isVerified(false)
                .build();
    }
}
