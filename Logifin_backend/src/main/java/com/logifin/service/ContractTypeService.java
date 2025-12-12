package com.logifin.service;

import com.logifin.dto.ContractTypeDTO;
import com.logifin.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for ContractType master data operations
 */
public interface ContractTypeService {

    ContractTypeDTO createContractType(ContractTypeDTO contractTypeDTO);

    ContractTypeDTO getContractTypeById(Long id);

    ContractTypeDTO getContractTypeByTypeName(String typeName);

    List<ContractTypeDTO> getAllContractTypes();

    List<ContractTypeDTO> getAllContractTypesOrderedByPartyCount();

    List<ContractTypeDTO> getContractTypesByPartyCount(Integer partyCount);

    PagedResponse<ContractTypeDTO> getAllContractTypes(Pageable pageable);

    ContractTypeDTO updateContractType(Long id, ContractTypeDTO contractTypeDTO);

    void deleteContractType(Long id);

    boolean existsByTypeName(String typeName);
}
