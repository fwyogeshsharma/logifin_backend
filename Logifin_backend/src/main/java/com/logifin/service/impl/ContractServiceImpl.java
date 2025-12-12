package com.logifin.service.impl;

import com.logifin.dto.*;
import com.logifin.entity.*;
import com.logifin.exception.BadRequestException;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.*;
import com.logifin.repository.specification.ContractSpecification;
import com.logifin.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContractServiceImpl implements ContractService {

    private static final String CACHE_CONTRACTS = "contracts";
    private static final String CACHE_CONTRACT_BY_ID = "contractById";
    private static final String CACHE_CONTRACT_BY_NUMBER = "contractByNumber";

    private final ContractRepository contractRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final LoanStageRepository loanStageRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    // ==================== CRUD Operations ====================

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACTS, allEntries = true),
            @CacheEvict(value = CACHE_CONTRACT_BY_NUMBER, allEntries = true)
    })
    public ContractResponse createContract(CreateContractRequest request) {
        log.debug("Creating contract: {}", request.getContractNumber());

        // Validate uniqueness
        if (StringUtils.hasText(request.getContractNumber())
                && contractRepository.existsByContractNumber(request.getContractNumber())) {
            throw new DuplicateResourceException("Contract", "contractNumber", request.getContractNumber());
        }

        // Fetch and validate related entities
        ContractType contractType = contractTypeRepository.findById(request.getContractTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ContractType", "id", request.getContractTypeId()));

        User contractManager = userRepository.findById(request.getContractManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getContractManagerId()));

        Company consignerCompany = companyRepository.findById(request.getConsignerCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getConsignerCompanyId()));

        LoanStage loanStage = null;
        if (request.getLoanStageId() != null) {
            loanStage = loanStageRepository.findById(request.getLoanStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("LoanStage", "id", request.getLoanStageId()));
        }

        // Build entity
        Contract contract = Contract.builder()
                .contractDocument(request.getContractDocument())
                .contractDocumentName(request.getContractDocumentName())
                .contractDocumentContentType(request.getContractDocumentContentType())
                .loanPercent(request.getLoanPercent())
                .ltv(request.getLtv())
                .penaltyRatio(request.getPenaltyRatio())
                .contractNumber(request.getContractNumber())
                .expiryDate(request.getExpiryDate())
                .contractType(contractType)
                .contractManager(contractManager)
                .consignerCompany(consignerCompany)
                .loanStage(loanStage)
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE")
                .build();

        // Save contract
        Contract savedContract = contractRepository.save(contract);

        // Add parties if provided
        if (request.getContractParties() != null && !request.getContractParties().isEmpty()) {
            for (ContractPartyDTO partyDTO : request.getContractParties()) {
                User partyUser = userRepository.findById(partyDTO.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", partyDTO.getUserId()));

                ContractParty contractParty = ContractParty.builder()
                        .contract(savedContract)
                        .user(partyUser)
                        .signedAt(partyDTO.getSignedAt())
                        .build();

                savedContract.addContractParty(contractParty);
            }
            savedContract = contractRepository.save(savedContract);
        }

        log.info("Contract created successfully with id: {}", savedContract.getId());
        return mapToResponse(savedContract);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CONTRACT_BY_ID, key = "#id", unless = "#result == null")
    public ContractResponse getContractById(Long id) {
        log.debug("Fetching contract by id: {}", id);
        Contract contract = contractRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CONTRACT_BY_NUMBER, key = "#contractNumber", unless = "#result == null")
    public ContractResponse getContractByContractNumber(String contractNumber) {
        log.debug("Fetching contract by contract number: {}", contractNumber);
        Contract contract = contractRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "contractNumber", contractNumber));
        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CONTRACTS, unless = "#result == null || #result.empty")
    public PagedResponse<ContractResponse> getAllContracts(Pageable pageable) {
        log.debug("Fetching all contracts with pagination");
        Page<Contract> contractsPage = contractRepository.findAllWithDetails(pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> searchContracts(ContractSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching contracts with criteria: {}", criteria);
        Specification<Contract> spec = ContractSpecification.fromCriteria(criteria);
        Page<Contract> contractsPage = contractRepository.findAll(spec, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACT_BY_ID, key = "#id"),
            @CacheEvict(value = CACHE_CONTRACT_BY_NUMBER, allEntries = true),
            @CacheEvict(value = CACHE_CONTRACTS, allEntries = true)
    })
    public ContractResponse updateContract(Long id, UpdateContractRequest request) {
        log.debug("Updating contract with id: {}", id);

        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));

        // Validate contract number uniqueness if changed
        if (StringUtils.hasText(request.getContractNumber())
                && !request.getContractNumber().equals(existingContract.getContractNumber())
                && contractRepository.existsByContractNumber(request.getContractNumber())) {
            throw new DuplicateResourceException("Contract", "contractNumber", request.getContractNumber());
        }

        // Update fields if provided
        if (StringUtils.hasText(request.getContractDocument())) {
            existingContract.setContractDocument(request.getContractDocument());
        }
        if (StringUtils.hasText(request.getContractDocumentName())) {
            existingContract.setContractDocumentName(request.getContractDocumentName());
        }
        if (StringUtils.hasText(request.getContractDocumentContentType())) {
            existingContract.setContractDocumentContentType(request.getContractDocumentContentType());
        }
        if (request.getLoanPercent() != null) {
            existingContract.setLoanPercent(request.getLoanPercent());
        }
        if (request.getLtv() != null) {
            existingContract.setLtv(request.getLtv());
        }
        if (request.getPenaltyRatio() != null) {
            existingContract.setPenaltyRatio(request.getPenaltyRatio());
        }
        if (StringUtils.hasText(request.getContractNumber())) {
            existingContract.setContractNumber(request.getContractNumber());
        }
        if (request.getExpiryDate() != null) {
            existingContract.setExpiryDate(request.getExpiryDate());
        }
        if (StringUtils.hasText(request.getStatus())) {
            existingContract.setStatus(request.getStatus());
        }

        // Update foreign key relationships if provided
        if (request.getContractTypeId() != null) {
            ContractType contractType = contractTypeRepository.findById(request.getContractTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ContractType", "id", request.getContractTypeId()));
            existingContract.setContractType(contractType);
        }

        if (request.getContractManagerId() != null) {
            User contractManager = userRepository.findById(request.getContractManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getContractManagerId()));
            existingContract.setContractManager(contractManager);
        }

        if (request.getConsignerCompanyId() != null) {
            Company consignerCompany = companyRepository.findById(request.getConsignerCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getConsignerCompanyId()));
            existingContract.setConsignerCompany(consignerCompany);
        }

        if (request.getLoanStageId() != null) {
            LoanStage loanStage = loanStageRepository.findById(request.getLoanStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("LoanStage", "id", request.getLoanStageId()));
            existingContract.setLoanStage(loanStage);
        }

        // Update contract parties if provided
        if (request.getContractParties() != null) {
            // Clear existing parties
            existingContract.getContractParties().clear();
            contractRepository.flush();

            // Add new parties
            for (ContractPartyDTO partyDTO : request.getContractParties()) {
                User partyUser = userRepository.findById(partyDTO.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", partyDTO.getUserId()));

                ContractParty contractParty = ContractParty.builder()
                        .contract(existingContract)
                        .user(partyUser)
                        .signedAt(partyDTO.getSignedAt())
                        .build();

                existingContract.addContractParty(contractParty);
            }
        }

        Contract updatedContract = contractRepository.save(existingContract);
        log.info("Contract updated successfully with id: {}", updatedContract.getId());
        return mapToResponse(updatedContract);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACT_BY_ID, key = "#id"),
            @CacheEvict(value = CACHE_CONTRACT_BY_NUMBER, allEntries = true),
            @CacheEvict(value = CACHE_CONTRACTS, allEntries = true)
    })
    public void deleteContract(Long id) {
        log.debug("Deleting contract with id: {}", id);
        if (!contractRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contract", "id", id);
        }
        contractRepository.deleteById(id);
        log.info("Contract deleted successfully with id: {}", id);
    }

    // ==================== Contract Parties Management ====================

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACT_BY_ID, key = "#contractId"),
            @CacheEvict(value = CACHE_CONTRACTS, allEntries = true)
    })
    public ContractResponse addPartyToContract(Long contractId, ContractPartyDTO partyDTO) {
        log.debug("Adding party {} to contract {}", partyDTO.getUserId(), contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));

        User partyUser = userRepository.findById(partyDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", partyDTO.getUserId()));

        if (contractPartyRepository.existsByContractIdAndUserId(contractId, partyDTO.getUserId())) {
            throw new DuplicateResourceException("ContractParty", "userId", partyDTO.getUserId());
        }

        ContractParty contractParty = ContractParty.builder()
                .contract(contract)
                .user(partyUser)
                .signedAt(partyDTO.getSignedAt())
                .build();

        contract.addContractParty(contractParty);
        Contract updatedContract = contractRepository.save(contract);
        log.info("Party added successfully to contract {}", contractId);
        return mapToResponse(updatedContract);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACT_BY_ID, key = "#contractId"),
            @CacheEvict(value = CACHE_CONTRACTS, allEntries = true)
    })
    public ContractResponse removePartyFromContract(Long contractId, Long userId) {
        log.debug("Removing party {} from contract {}", userId, contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));

        ContractParty contractParty = contractPartyRepository.findByContractIdAndUserId(contractId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractParty", "userId", userId));

        contract.removeContractParty(contractParty);
        contractPartyRepository.delete(contractParty);
        Contract updatedContract = contractRepository.save(contract);
        log.info("Party removed successfully from contract {}", contractId);
        return mapToResponse(updatedContract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractPartyDTO> getContractParties(Long contractId) {
        log.debug("Fetching parties for contract {}", contractId);
        List<ContractParty> parties = contractPartyRepository.findByContractId(contractId);
        return parties.stream()
                .map(this::mapPartyToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Query Methods ====================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByStatus(String status, Pageable pageable) {
        log.debug("Fetching contracts by status: {}", status);
        Page<Contract> contractsPage = contractRepository.findByStatus(status, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByContractType(Long contractTypeId, Pageable pageable) {
        log.debug("Fetching contracts by contract type: {}", contractTypeId);
        Page<Contract> contractsPage = contractRepository.findByContractTypeId(contractTypeId, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByManager(Long managerId, Pageable pageable) {
        log.debug("Fetching contracts by manager: {}", managerId);
        Page<Contract> contractsPage = contractRepository.findByContractManagerId(managerId, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByCompany(Long companyId, Pageable pageable) {
        log.debug("Fetching contracts by company: {}", companyId);
        Page<Contract> contractsPage = contractRepository.findByConsignerCompanyId(companyId, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByLoanStage(Long loanStageId, Pageable pageable) {
        log.debug("Fetching contracts by loan stage: {}", loanStageId);
        Page<Contract> contractsPage = contractRepository.findByLoanStageId(loanStageId, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByPartyUser(Long userId, Pageable pageable) {
        log.debug("Fetching contracts by party user: {}", userId);
        Page<Contract> contractsPage = contractRepository.findContractsByPartyUserId(userId, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getContractsByUserInvolvement(Long userId, Pageable pageable) {
        log.debug("Fetching contracts by user involvement: {}", userId);
        Page<Contract> contractsPage = contractRepository.findContractsByUserInvolvement(userId, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> getExpiringContracts(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        log.debug("Fetching contracts expiring between {} and {}", fromDate, toDate);
        Page<Contract> contractsPage = contractRepository.findByExpiryDateBetween(fromDate, toDate, pageable);
        List<ContractResponse> content = contractsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(contractsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsExpiringSoon(int daysAhead) {
        log.debug("Fetching contracts expiring within {} days", daysAhead);
        LocalDate expiryDate = LocalDate.now().plusDays(daysAhead);
        List<Contract> contracts = contractRepository.findContractsExpiringSoon(expiryDate);
        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getExpiredContracts() {
        log.debug("Fetching expired contracts");
        List<Contract> contracts = contractRepository.findExpiredContracts();
        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== Statistics ====================

    @Override
    @Transactional(readOnly = true)
    public long countContractsByStatus(String status) {
        return contractRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countContractsByType(Long contractTypeId) {
        return contractRepository.countByContractTypeId(contractTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countContractsByLoanStage(Long loanStageId) {
        return contractRepository.countByLoanStageId(loanStageId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countContractsByCompany(Long companyId) {
        return contractRepository.countByConsignerCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countContractsByManager(Long managerId) {
        return contractRepository.countByContractManagerId(managerId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageLoanPercent() {
        return contractRepository.getAverageLoanPercent();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageLtv() {
        return contractRepository.getAverageLtv();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAveragePenaltyRatio() {
        return contractRepository.getAveragePenaltyRatio();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getContractCountByType() {
        List<Object[]> results = contractRepository.getContractCountByType();
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (Object[] result : results) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getContractCountByStatus() {
        List<Object[]> results = contractRepository.getContractCountByStatus();
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (Object[] result : results) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getContractCountByLoanStage() {
        List<Object[]> results = contractRepository.getContractCountByLoanStage();
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (Object[] result : results) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        return countMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopContractManagers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = contractRepository.getTopContractManagers(pageable);
        List<Map<String, Object>> topManagers = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> manager = new HashMap<>();
            manager.put("id", result[0]);
            manager.put("firstName", result[1]);
            manager.put("lastName", result[2]);
            manager.put("contractCount", result[3]);
            topManagers.add(manager);
        }
        return topManagers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopConsignerCompanies(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = contractRepository.getTopConsignerCompanies(pageable);
        List<Map<String, Object>> topCompanies = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> company = new HashMap<>();
            company.put("id", result[0]);
            company.put("name", result[1]);
            company.put("contractCount", result[2]);
            topCompanies.add(company);
        }
        return topCompanies;
    }

    // ==================== Business Logic ====================

    @Override
    @Transactional
    public void expireOverdueContracts() {
        log.info("Expiring overdue contracts");
        List<Contract> expiredContracts = contractRepository.findExpiredContracts();
        for (Contract contract : expiredContracts) {
            contract.setStatus("EXPIRED");
            contractRepository.save(contract);
        }
        log.info("Expired {} contracts", expiredContracts.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isContractExpired(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
        return contract.getExpiryDate().isBefore(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserPartyToContract(Long contractId, Long userId) {
        return contractPartyRepository.existsByContractIdAndUserId(contractId, userId);
    }

    // ==================== Mapping Methods ====================

    private ContractResponse mapToResponse(Contract contract) {
        ContractResponse.ContractResponseBuilder builder = ContractResponse.builder()
                .id(contract.getId())
                .contractDocument(contract.getContractDocument())
                .contractDocumentName(contract.getContractDocumentName())
                .contractDocumentContentType(contract.getContractDocumentContentType())
                .loanPercent(contract.getLoanPercent())
                .ltv(contract.getLtv())
                .penaltyRatio(contract.getPenaltyRatio())
                .contractNumber(contract.getContractNumber())
                .expiryDate(contract.getExpiryDate())
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .version(contract.getVersion());

        // Contract Type
        if (contract.getContractType() != null) {
            builder.contractTypeId(contract.getContractType().getId())
                    .contractTypeName(contract.getContractType().getTypeName())
                    .partyCount(contract.getContractType().getPartyCount());
        }

        // Contract Manager
        if (contract.getContractManager() != null) {
            builder.contractManagerId(contract.getContractManager().getId())
                    .contractManagerName(contract.getContractManager().getFirstName() + " " + contract.getContractManager().getLastName())
                    .contractManagerEmail(contract.getContractManager().getEmail());
        }

        // Consigner Company
        if (contract.getConsignerCompany() != null) {
            builder.consignerCompanyId(contract.getConsignerCompany().getId())
                    .consignerCompanyName(contract.getConsignerCompany().getName())
                    .consignerCompanyGst(contract.getConsignerCompany().getGstNumber());
        }

        // Loan Stage
        if (contract.getLoanStage() != null) {
            builder.loanStageId(contract.getLoanStage().getId())
                    .loanStageName(contract.getLoanStage().getStageName())
                    .loanStageOrder(contract.getLoanStage().getStageOrder());
        }

        // Created By
        if (contract.getCreatedBy() != null) {
            builder.createdByUserId(contract.getCreatedBy().getId())
                    .createdByUserName(contract.getCreatedBy().getFirstName() + " " + contract.getCreatedBy().getLastName());
        }

        // Contract Parties
        if (contract.getContractParties() != null && !contract.getContractParties().isEmpty()) {
            List<ContractPartyDTO> parties = contract.getContractParties().stream()
                    .map(this::mapPartyToDTO)
                    .collect(Collectors.toList());
            builder.contractParties(parties);
        }

        return builder.build();
    }

    private ContractPartyDTO mapPartyToDTO(ContractParty contractParty) {
        ContractPartyDTO.ContractPartyDTOBuilder builder = ContractPartyDTO.builder()
                .id(contractParty.getId())
                .userId(contractParty.getUser().getId())
                .signedAt(contractParty.getSignedAt());

        if (contractParty.getUser() != null) {
            builder.userName(contractParty.getUser().getFirstName() + " " + contractParty.getUser().getLastName())
                    .userEmail(contractParty.getUser().getEmail());
        }

        return builder.build();
    }
}
