package com.logifin.service;

import com.logifin.dto.CompanyDTO;
import com.logifin.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompanyService {

    CompanyDTO createCompany(CompanyDTO companyDTO);

    CompanyDTO getCompanyById(Long id);

    CompanyDTO getCompanyByEmail(String email);

    CompanyDTO getCompanyByGstNumber(String gstNumber);

    List<CompanyDTO> getAllCompanies();

    List<CompanyDTO> getActiveCompanies();

    List<CompanyDTO> getVerifiedCompanies();

    List<CompanyDTO> getPendingVerificationCompanies();

    CompanyDTO updateCompany(Long id, CompanyDTO companyDTO);

    void deleteCompany(Long id);

    void activateCompany(Long id);

    void deactivateCompany(Long id);

    void verifyCompany(Long id, Long verifiedByUserId);

    // Paginated methods
    PagedResponse<CompanyDTO> getAllCompanies(Pageable pageable);

    PagedResponse<CompanyDTO> getActiveCompanies(Pageable pageable);

    PagedResponse<CompanyDTO> getInactiveCompanies(Pageable pageable);

    PagedResponse<CompanyDTO> getVerifiedCompanies(Pageable pageable);

    PagedResponse<CompanyDTO> getPendingVerificationCompanies(Pageable pageable);

    PagedResponse<CompanyDTO> searchCompanies(String keyword, Pageable pageable);

    PagedResponse<CompanyDTO> getCompaniesByCity(String city, Pageable pageable);

    PagedResponse<CompanyDTO> getCompaniesByState(String state, Pageable pageable);
}
