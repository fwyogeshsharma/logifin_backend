package com.logifin.service.impl;

import com.logifin.dto.CompanyAdminUserDTO;
import com.logifin.dto.GetCompanyAdminResponse;
import com.logifin.dto.UpdateCompanyAdminRequest;
import com.logifin.entity.Company;
import com.logifin.entity.CompanyAdmin;
import com.logifin.entity.User;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.CompanyAdminRepository;
import com.logifin.repository.CompanyRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.CompanyAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CompanyAdminService.
 * Manages company admin/owner operations separate from role-based permissions.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanyAdminServiceImpl implements CompanyAdminService {

    private final CompanyAdminRepository companyAdminRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public GetCompanyAdminResponse getCompanyAdmin(Long companyId) {
        log.debug("Fetching company admin for company ID: {}", companyId);

        // Verify company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Find company admin
        CompanyAdmin companyAdmin = companyAdminRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyAdmin", "companyId", companyId));

        User adminUser = companyAdmin.getUser();

        // Build response
        CompanyAdminUserDTO adminUserDTO = CompanyAdminUserDTO.builder()
                .userId(adminUser.getId())
                .name(adminUser.getFirstName() + " " + adminUser.getLastName())
                .email(adminUser.getEmail())
                .phone(adminUser.getPhone())
                .build();

        return GetCompanyAdminResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .adminUser(adminUserDTO)
                .assignedAt(companyAdmin.getCreatedAt())
                .updatedAt(companyAdmin.getUpdatedAt())
                .build();
    }

    @Override
    public void updateCompanyAdmin(UpdateCompanyAdminRequest request) {
        log.debug("Updating company admin for company ID: {} to user ID: {}",
                request.getCompanyId(), request.getNewAdminUserId());

        // Validate company exists
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        // Validate new admin user exists
        User newAdminUser = userRepository.findById(request.getNewAdminUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getNewAdminUserId()));

        // Validate new admin user belongs to the same company
        if (newAdminUser.getCompany() == null || !newAdminUser.getCompany().getId().equals(request.getCompanyId())) {
            throw new IllegalArgumentException("User does not belong to this company");
        }

        // Find existing company admin or create new
        CompanyAdmin companyAdmin = companyAdminRepository.findByCompanyId(request.getCompanyId())
                .orElse(CompanyAdmin.builder()
                        .company(company)
                        .build());

        // Update the admin user
        companyAdmin.setUser(newAdminUser);
        companyAdminRepository.save(companyAdmin);

        log.info("Company admin updated successfully for company ID: {} to user ID: {}",
                request.getCompanyId(), request.getNewAdminUserId());
    }

    @Override
    public void assignFirstUserAsAdmin(Company company, User user) {
        log.debug("Assigning first user {} as admin for company {}", user.getId(), company.getId());

        // Check if company already has an admin
        if (companyAdminRepository.existsByCompanyId(company.getId())) {
            log.debug("Company {} already has an admin, skipping assignment", company.getId());
            return;
        }

        // Create new company admin entry
        CompanyAdmin companyAdmin = CompanyAdmin.builder()
                .company(company)
                .user(user)
                .build();

        companyAdminRepository.save(companyAdmin);

        log.info("User {} assigned as company admin for company {}", user.getId(), company.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCompanyAdmin(Long companyId) {
        return companyAdminRepository.existsByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserCompanyAdmin(Long userId) {
        return companyAdminRepository.existsByUserId(userId);
    }
}
