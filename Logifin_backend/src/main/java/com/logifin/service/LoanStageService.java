package com.logifin.service;

import com.logifin.dto.LoanStageDTO;
import com.logifin.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for LoanStage master data operations
 */
public interface LoanStageService {

    LoanStageDTO createLoanStage(LoanStageDTO loanStageDTO);

    LoanStageDTO getLoanStageById(Long id);

    LoanStageDTO getLoanStageByStageName(String stageName);

    List<LoanStageDTO> getAllLoanStages();

    PagedResponse<LoanStageDTO> getAllLoanStages(Pageable pageable);

    LoanStageDTO updateLoanStage(Long id, LoanStageDTO loanStageDTO);

    void deleteLoanStage(Long id);

    boolean existsByStageName(String stageName);

    boolean existsByStageOrder(Integer stageOrder);
}
