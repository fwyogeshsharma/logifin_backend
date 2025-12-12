package com.logifin.repository;

import com.logifin.entity.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ContractType entity - Master data
 */
@Repository
public interface ContractTypeRepository extends JpaRepository<ContractType, Long> {

    /**
     * Find contract type by type name
     */
    Optional<ContractType> findByTypeName(String typeName);

    /**
     * Find contract types by party count
     */
    List<ContractType> findByPartyCount(Integer partyCount);

    /**
     * Check if a type name exists
     */
    boolean existsByTypeName(String typeName);

    /**
     * Get all contract types ordered by party count
     */
    @Query("SELECT ct FROM ContractType ct ORDER BY ct.partyCount ASC")
    List<ContractType> findAllOrderedByPartyCount();
}
