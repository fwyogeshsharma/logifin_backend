package com.logifin.service.impl;

import com.logifin.dto.ContractTypeDTO;
import com.logifin.dto.PagedResponse;
import com.logifin.entity.ContractType;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.ContractTypeRepository;
import com.logifin.service.ContractTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContractTypeServiceImpl implements ContractTypeService {

    private static final String CACHE_CONTRACT_TYPES = "contractTypes";
    private static final String CACHE_CONTRACT_TYPE_BY_ID = "contractTypeById";
    private static final String CACHE_CONTRACT_TYPE_BY_NAME = "contractTypeByName";

    private final ContractTypeRepository contractTypeRepository;

    @Override
    @CacheEvict(value = CACHE_CONTRACT_TYPES, allEntries = true)
    public ContractTypeDTO createContractType(ContractTypeDTO contractTypeDTO) {
        log.debug("Creating contract type: {}", contractTypeDTO.getTypeName());

        if (contractTypeRepository.existsByTypeName(contractTypeDTO.getTypeName())) {
            throw new DuplicateResourceException("ContractType", "typeName", contractTypeDTO.getTypeName());
        }

        ContractType contractType = mapToEntity(contractTypeDTO);
        ContractType savedContractType = contractTypeRepository.save(contractType);
        return mapToDTO(savedContractType);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CONTRACT_TYPE_BY_ID, key = "#id", unless = "#result == null")
    public ContractTypeDTO getContractTypeById(Long id) {
        log.debug("Fetching contract type by id: {}", id);
        ContractType contractType = contractTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContractType", "id", id));
        return mapToDTO(contractType);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CONTRACT_TYPE_BY_NAME, key = "#typeName", unless = "#result == null")
    public ContractTypeDTO getContractTypeByTypeName(String typeName) {
        log.debug("Fetching contract type by name: {}", typeName);
        ContractType contractType = contractTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new ResourceNotFoundException("ContractType", "typeName", typeName));
        return mapToDTO(contractType);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CONTRACT_TYPES, unless = "#result == null || #result.isEmpty()")
    public List<ContractTypeDTO> getAllContractTypes() {
        log.debug("Fetching all contract types");
        return contractTypeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractTypeDTO> getAllContractTypesOrderedByPartyCount() {
        log.debug("Fetching all contract types ordered by party count");
        return contractTypeRepository.findAllOrderedByPartyCount().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractTypeDTO> getContractTypesByPartyCount(Integer partyCount) {
        log.debug("Fetching contract types by party count: {}", partyCount);
        return contractTypeRepository.findByPartyCount(partyCount).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractTypeDTO> getAllContractTypes(Pageable pageable) {
        log.debug("Fetching all contract types with pagination");
        Page<ContractType> contractTypesPage = contractTypeRepository.findAll(pageable);
        List<ContractTypeDTO> content = contractTypesPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(contractTypesPage, content);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACT_TYPE_BY_ID, key = "#id"),
            @CacheEvict(value = CACHE_CONTRACT_TYPE_BY_NAME, allEntries = true),
            @CacheEvict(value = CACHE_CONTRACT_TYPES, allEntries = true)
    })
    public ContractTypeDTO updateContractType(Long id, ContractTypeDTO contractTypeDTO) {
        log.debug("Updating contract type with id: {}", id);
        ContractType existingContractType = contractTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContractType", "id", id));

        if (!existingContractType.getTypeName().equals(contractTypeDTO.getTypeName())
                && contractTypeRepository.existsByTypeName(contractTypeDTO.getTypeName())) {
            throw new DuplicateResourceException("ContractType", "typeName", contractTypeDTO.getTypeName());
        }

        existingContractType.setTypeName(contractTypeDTO.getTypeName());
        existingContractType.setDescription(contractTypeDTO.getDescription());
        existingContractType.setPartyCount(contractTypeDTO.getPartyCount());

        ContractType updatedContractType = contractTypeRepository.save(existingContractType);
        return mapToDTO(updatedContractType);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_CONTRACT_TYPE_BY_ID, key = "#id"),
            @CacheEvict(value = CACHE_CONTRACT_TYPE_BY_NAME, allEntries = true),
            @CacheEvict(value = CACHE_CONTRACT_TYPES, allEntries = true)
    })
    public void deleteContractType(Long id) {
        log.debug("Deleting contract type with id: {}", id);
        if (!contractTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("ContractType", "id", id);
        }
        contractTypeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTypeName(String typeName) {
        return contractTypeRepository.existsByTypeName(typeName);
    }

    // ==================== Mapping Methods ====================

    private ContractTypeDTO mapToDTO(ContractType contractType) {
        return ContractTypeDTO.builder()
                .id(contractType.getId())
                .typeName(contractType.getTypeName())
                .description(contractType.getDescription())
                .partyCount(contractType.getPartyCount())
                .build();
    }

    private ContractType mapToEntity(ContractTypeDTO dto) {
        return ContractType.builder()
                .typeName(dto.getTypeName())
                .description(dto.getDescription())
                .partyCount(dto.getPartyCount())
                .build();
    }
}
