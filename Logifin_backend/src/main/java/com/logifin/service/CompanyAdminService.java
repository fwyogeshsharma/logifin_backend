package com.logifin.service;

import com.logifin.dto.GetCompanyAdminResponse;
import com.logifin.dto.UpdateCompanyAdminRequest;
import com.logifin.entity.Company;
import com.logifin.entity.User;

/**
 * Service interface for company admin management.
 * Handles company ownership operations separate from role-based permissions.
 */
public interface CompanyAdminService {

    /**
     * Get the company admin for a given company.
     * @param companyId the company ID
     * @return GetCompanyAdminResponse containing admin details
     */
    GetCompanyAdminResponse getCompanyAdmin(Long companyId);

    /**
     * Update/change the company admin.
     * @param request the update request containing companyId and newAdminUserId
     */
    void updateCompanyAdmin(UpdateCompanyAdminRequest request);

    /**
     * Assign the first user as company admin.
     * Called during registration when the first user registers under a company.
     * @param company the company
     * @param user the user to be assigned as admin
     */
    void assignFirstUserAsAdmin(Company company, User user);

    /**
     * Check if a company has an admin assigned.
     * @param companyId the company ID
     * @return true if admin exists, false otherwise
     */
    boolean hasCompanyAdmin(Long companyId);

    /**
     * Check if a user is a company admin.
     * @param userId the user ID
     * @return true if user is an admin, false otherwise
     */
    boolean isUserCompanyAdmin(Long userId);
}
