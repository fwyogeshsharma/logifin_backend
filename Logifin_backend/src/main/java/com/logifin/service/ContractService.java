package com.logifin.service;

import com.logifin.dto.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Contract operations
 */
public interface ContractService {

    // ==================== CRUD Operations ====================

    ContractResponse createContract(CreateContractRequest request, Long createdByUserId);

    ContractResponse getContractById(Long id);

    ContractResponse getContractByContractNumber(String contractNumber);

    PagedResponse<ContractResponse> getAllContracts(Pageable pageable);

    PagedResponse<ContractResponse> searchContracts(ContractSearchCriteria criteria, Pageable pageable);

    ContractResponse updateContract(Long id, UpdateContractRequest request);

    void deleteContract(Long id);

    // ==================== Contract Parties Management ====================

    ContractResponse addPartyToContract(Long contractId, ContractPartyDTO partyDTO);

    ContractResponse removePartyFromContract(Long contractId, Long userId);

    List<ContractPartyDTO> getContractParties(Long contractId);

    // ==================== Query Methods ====================

    PagedResponse<ContractResponse> getContractsByStatus(String status, Pageable pageable);

    PagedResponse<ContractResponse> getContractsByContractType(Long contractTypeId, Pageable pageable);

    PagedResponse<ContractResponse> getContractsByManager(Long managerId, Pageable pageable);

    PagedResponse<ContractResponse> getContractsByCompany(Long companyId, Pageable pageable);

    PagedResponse<ContractResponse> getContractsByLoanStage(Long loanStageId, Pageable pageable);

    PagedResponse<ContractResponse> getContractsByPartyUser(Long userId, Pageable pageable);

    PagedResponse<ContractResponse> getContractsByUserInvolvement(Long userId, Pageable pageable);

    PagedResponse<ContractResponse> getExpiringContracts(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    List<ContractResponse> getContractsExpiringSoon(int daysAhead);

    List<ContractResponse> getExpiredContracts();

    // ==================== Statistics ====================

    long countContractsByStatus(String status);

    long countContractsByType(Long contractTypeId);

    long countContractsByLoanStage(Long loanStageId);

    long countContractsByCompany(Long companyId);

    long countContractsByManager(Long managerId);

    BigDecimal getAverageLoanPercent();

    BigDecimal getAverageLtv();

    BigDecimal getAveragePenaltyRatio();

    Map<String, Long> getContractCountByType();

    Map<String, Long> getContractCountByStatus();

    Map<String, Long> getContractCountByLoanStage();

    List<Map<String, Object>> getTopContractManagers(int limit);

    List<Map<String, Object>> getTopConsignerCompanies(int limit);

    // ==================== Business Logic ====================

    void expireOverdueContracts();

    boolean isContractExpired(Long contractId);

    boolean isUserPartyToContract(Long contractId, Long userId);
}
