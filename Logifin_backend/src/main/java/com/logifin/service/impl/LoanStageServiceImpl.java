package com.logifin.service.impl;

import com.logifin.dto.LoanStageDTO;
import com.logifin.dto.PagedResponse;
import com.logifin.entity.LoanStage;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.LoanStageRepository;
import com.logifin.service.LoanStageService;
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
public class LoanStageServiceImpl implements LoanStageService {

    private static final String CACHE_LOAN_STAGES = "loanStages";
    private static final String CACHE_LOAN_STAGE_BY_ID = "loanStageById";
    private static final String CACHE_LOAN_STAGE_BY_NAME = "loanStageByName";

    private final LoanStageRepository loanStageRepository;

    @Override
    @CacheEvict(value = CACHE_LOAN_STAGES, allEntries = true)
    public LoanStageDTO createLoanStage(LoanStageDTO loanStageDTO) {
        log.debug("Creating loan stage: {}", loanStageDTO.getStageName());

        if (loanStageRepository.existsByStageName(loanStageDTO.getStageName())) {
            throw new DuplicateResourceException("LoanStage", "stageName", loanStageDTO.getStageName());
        }

        if (loanStageRepository.existsByStageOrder(loanStageDTO.getStageOrder())) {
            throw new DuplicateResourceException("LoanStage", "stageOrder", loanStageDTO.getStageOrder());
        }

        LoanStage loanStage = mapToEntity(loanStageDTO);
        LoanStage savedLoanStage = loanStageRepository.save(loanStage);
        return mapToDTO(savedLoanStage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LOAN_STAGE_BY_ID, key = "#id", unless = "#result == null")
    public LoanStageDTO getLoanStageById(Long id) {
        log.debug("Fetching loan stage by id: {}", id);
        LoanStage loanStage = loanStageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanStage", "id", id));
        return mapToDTO(loanStage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LOAN_STAGE_BY_NAME, key = "#stageName", unless = "#result == null")
    public LoanStageDTO getLoanStageByStageName(String stageName) {
        log.debug("Fetching loan stage by name: {}", stageName);
        LoanStage loanStage = loanStageRepository.findByStageName(stageName)
                .orElseThrow(() -> new ResourceNotFoundException("LoanStage", "stageName", stageName));
        return mapToDTO(loanStage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LOAN_STAGES, unless = "#result == null || #result.isEmpty()")
    public List<LoanStageDTO> getAllLoanStages() {
        log.debug("Fetching all loan stages");
        return loanStageRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LoanStageDTO> getAllLoanStages(Pageable pageable) {
        log.debug("Fetching all loan stages with pagination");
        Page<LoanStage> loanStagesPage = loanStageRepository.findAll(pageable);
        List<LoanStageDTO> content = loanStagesPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(loanStagesPage, content);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_LOAN_STAGE_BY_ID, key = "#id"),
            @CacheEvict(value = CACHE_LOAN_STAGE_BY_NAME, allEntries = true),
            @CacheEvict(value = CACHE_LOAN_STAGES, allEntries = true)
    })
    public LoanStageDTO updateLoanStage(Long id, LoanStageDTO loanStageDTO) {
        log.debug("Updating loan stage with id: {}", id);
        LoanStage existingLoanStage = loanStageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanStage", "id", id));

        if (!existingLoanStage.getStageName().equals(loanStageDTO.getStageName())
                && loanStageRepository.existsByStageName(loanStageDTO.getStageName())) {
            throw new DuplicateResourceException("LoanStage", "stageName", loanStageDTO.getStageName());
        }

        if (!existingLoanStage.getStageOrder().equals(loanStageDTO.getStageOrder())
                && loanStageRepository.existsByStageOrder(loanStageDTO.getStageOrder())) {
            throw new DuplicateResourceException("LoanStage", "stageOrder", loanStageDTO.getStageOrder());
        }

        existingLoanStage.setStageName(loanStageDTO.getStageName());
        existingLoanStage.setDescription(loanStageDTO.getDescription());
        existingLoanStage.setStageOrder(loanStageDTO.getStageOrder());

        LoanStage updatedLoanStage = loanStageRepository.save(existingLoanStage);
        return mapToDTO(updatedLoanStage);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_LOAN_STAGE_BY_ID, key = "#id"),
            @CacheEvict(value = CACHE_LOAN_STAGE_BY_NAME, allEntries = true),
            @CacheEvict(value = CACHE_LOAN_STAGES, allEntries = true)
    })
    public void deleteLoanStage(Long id) {
        log.debug("Deleting loan stage with id: {}", id);
        if (!loanStageRepository.existsById(id)) {
            throw new ResourceNotFoundException("LoanStage", "id", id);
        }
        loanStageRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByStageName(String stageName) {
        return loanStageRepository.existsByStageName(stageName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByStageOrder(Integer stageOrder) {
        return loanStageRepository.existsByStageOrder(stageOrder);
    }

    // ==================== Mapping Methods ====================

    private LoanStageDTO mapToDTO(LoanStage loanStage) {
        return LoanStageDTO.builder()
                .id(loanStage.getId())
                .stageName(loanStage.getStageName())
                .description(loanStage.getDescription())
                .stageOrder(loanStage.getStageOrder())
                .build();
    }

    private LoanStage mapToEntity(LoanStageDTO dto) {
        return LoanStage.builder()
                .stageName(dto.getStageName())
                .description(dto.getDescription())
                .stageOrder(dto.getStageOrder())
                .build();
    }
}
