package com.logifin.repository;

import com.logifin.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Contract entity with JPA Specification support for dynamic filtering.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    // ==================== Basic CRUD with EntityGraph ====================

    /**
     * Find contract by ID with eager loading of related entities
     */
    @EntityGraph(attributePaths = {"contractType", "contractManager", "consignerCompany", "loanStage", "createdBy"})
    Optional<Contract> findWithDetailsById(Long id);

    /**
     * Find all contracts with eager loading (for caching)
     */
    @EntityGraph(attributePaths = {"contractType", "contractManager", "consignerCompany", "loanStage"})
    @Query("SELECT c FROM Contract c")
    Page<Contract> findAllWithDetails(Pageable pageable);

    // ==================== Find by Foreign Keys ====================

    /**
     * Find contracts by contract type
     */
    Page<Contract> findByContractTypeId(Long contractTypeId, Pageable pageable);

    /**
     * Find contracts by contract manager
     */
    Page<Contract> findByContractManagerId(Long contractManagerId, Pageable pageable);

    /**
     * Find contracts by consigner company
     */
    Page<Contract> findByConsignerCompanyId(Long consignerCompanyId, Pageable pageable);

    /**
     * Find contracts by loan stage
     */
    Page<Contract> findByLoanStageId(Long loanStageId, Pageable pageable);

    /**
     * Find contracts by created by user
     */
    Page<Contract> findByCreatedById(Long createdByUserId, Pageable pageable);

    /**
     * Find contracts by status
     */
    Page<Contract> findByStatus(String status, Pageable pageable);

    // ==================== Search Queries ====================

    /**
     * Find contract by contract number
     */
    Optional<Contract> findByContractNumber(String contractNumber);

    /**
     * Check if contract number exists
     */
    boolean existsByContractNumber(String contractNumber);

    /**
     * Search contracts by keyword across multiple fields
     */
    @EntityGraph(attributePaths = {"contractType", "contractManager", "consignerCompany", "loanStage"})
    @Query("SELECT c FROM Contract c " +
           "LEFT JOIN c.contractManager cm " +
           "LEFT JOIN c.consignerCompany cc " +
           "WHERE LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cm.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cm.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cc.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Contract> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find contracts expiring within a date range
     */
    @Query("SELECT c FROM Contract c WHERE c.expiryDate BETWEEN :fromDate AND :toDate")
    Page<Contract> findByExpiryDateBetween(@Param("fromDate") LocalDate fromDate,
                                            @Param("toDate") LocalDate toDate,
                                            Pageable pageable);

    /**
     * Find contracts created within a date range
     */
    @Query("SELECT c FROM Contract c WHERE CAST(c.createdAt AS date) BETWEEN :fromDate AND :toDate")
    Page<Contract> findByCreatedDateBetween(@Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate,
                                             Pageable pageable);

    /**
     * Find expired contracts
     */
    @Query("SELECT c FROM Contract c WHERE c.expiryDate < CURRENT_DATE AND c.status = 'ACTIVE'")
    List<Contract> findExpiredContracts();

    /**
     * Find contracts expiring soon (within specified days)
     */
    @Query("SELECT c FROM Contract c WHERE c.expiryDate BETWEEN CURRENT_DATE AND :expiryDate AND c.status = 'ACTIVE'")
    List<Contract> findContractsExpiringSoon(@Param("expiryDate") LocalDate expiryDate);

    // ==================== Statistics & Aggregation Queries ====================

    /**
     * Count contracts by status
     */
    long countByStatus(String status);

    /**
     * Count contracts by contract type
     */
    long countByContractTypeId(Long contractTypeId);

    /**
     * Count contracts by loan stage
     */
    long countByLoanStageId(Long loanStageId);

    /**
     * Count contracts by consigner company
     */
    long countByConsignerCompanyId(Long consignerCompanyId);

    /**
     * Count contracts by manager
     */
    long countByContractManagerId(Long contractManagerId);

    /**
     * Get average loan percent
     */
    @Query("SELECT COALESCE(AVG(c.loanPercent), 0) FROM Contract c")
    BigDecimal getAverageLoanPercent();

    /**
     * Get average LTV
     */
    @Query("SELECT COALESCE(AVG(c.ltv), 0) FROM Contract c")
    BigDecimal getAverageLtv();

    /**
     * Get average penalty ratio
     */
    @Query("SELECT COALESCE(AVG(c.penaltyRatio), 0) FROM Contract c")
    BigDecimal getAveragePenaltyRatio();

    /**
     * Get contracts count by type
     */
    @Query("SELECT ct.typeName, COUNT(c) FROM Contract c JOIN c.contractType ct GROUP BY ct.typeName ORDER BY COUNT(c) DESC")
    List<Object[]> getContractCountByType();

    /**
     * Get contracts count by status
     */
    @Query("SELECT c.status, COUNT(c) FROM Contract c GROUP BY c.status ORDER BY COUNT(c) DESC")
    List<Object[]> getContractCountByStatus();

    /**
     * Get contracts count by loan stage
     */
    @Query("SELECT ls.stageName, COUNT(c) FROM Contract c JOIN c.loanStage ls GROUP BY ls.stageName ORDER BY ls.stageOrder ASC")
    List<Object[]> getContractCountByLoanStage();

    /**
     * Get top contract managers by contract count
     */
    @Query("SELECT c.contractManager.id, c.contractManager.firstName, c.contractManager.lastName, COUNT(c) " +
           "FROM Contract c GROUP BY c.contractManager.id, c.contractManager.firstName, c.contractManager.lastName " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getTopContractManagers(Pageable pageable);

    /**
     * Get top consigner companies by contract count
     */
    @Query("SELECT c.consignerCompany.id, c.consignerCompany.name, COUNT(c) " +
           "FROM Contract c GROUP BY c.consignerCompany.id, c.consignerCompany.name " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getTopConsignerCompanies(Pageable pageable);

    // ==================== User-specific Queries ====================

    /**
     * Find contracts where user is a party (via ContractParty)
     */
    @EntityGraph(attributePaths = {"contractType", "contractManager", "consignerCompany", "loanStage"})
    @Query("SELECT DISTINCT c FROM Contract c JOIN c.contractParties cp WHERE cp.user.id = :userId")
    Page<Contract> findContractsByPartyUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find contracts related to a user (manager or party)
     */
    @EntityGraph(attributePaths = {"contractType", "contractManager", "consignerCompany", "loanStage"})
    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN c.contractParties cp " +
           "WHERE c.contractManager.id = :userId OR cp.user.id = :userId")
    Page<Contract> findContractsByUserInvolvement(@Param("userId") Long userId, Pageable pageable);
}
