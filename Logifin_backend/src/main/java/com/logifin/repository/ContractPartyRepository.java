package com.logifin.repository;

import com.logifin.entity.Contract;
import com.logifin.entity.ContractParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ContractParty entity
 */
@Repository
public interface ContractPartyRepository extends JpaRepository<ContractParty, Long> {

    /**
     * Find all parties for a specific contract
     */
    List<ContractParty> findByContractId(Long contractId);

    /**
     * Find all contracts for a specific user
     */
    List<ContractParty> findByUserId(Long userId);

    /**
     * Find a specific party in a contract
     */
    Optional<ContractParty> findByContractIdAndUserId(Long contractId, Long userId);

    /**
     * Check if a user is a party to a contract
     */
    boolean existsByContractIdAndUserId(Long contractId, Long userId);

    /**
     * Count parties in a contract
     */
    long countByContractId(Long contractId);

    /**
     * Count contracts for a user
     */
    long countByUserId(Long userId);

    /**
     * Delete all parties for a contract
     */
    void deleteByContractId(Long contractId);

    /**
     * Get all signed parties for a contract
     */
    @Query("SELECT cp FROM ContractParty cp WHERE cp.contract.id = :contractId AND cp.signedAt IS NOT NULL")
    List<ContractParty> findSignedPartiesByContractId(@Param("contractId") Long contractId);

    /**
     * Get all unsigned parties for a contract
     */
    @Query("SELECT cp FROM ContractParty cp WHERE cp.contract.id = :contractId AND cp.signedAt IS NULL")
    List<ContractParty> findUnsignedPartiesByContractId(@Param("contractId") Long contractId);

    /**
     * Count signed parties for a contract
     */
    @Query("SELECT COUNT(cp) FROM ContractParty cp WHERE cp.contract.id = :contractId AND cp.signedAt IS NOT NULL")
    long countSignedPartiesByContractId(@Param("contractId") Long contractId);

    /**
     * Find contracts that have all three specified users as parties.
     * Used to find the contract between lender, transporter, and consigner for trip financing.
     * Returns active, non-expired contracts ordered by expiry date (furthest expiry first).
     */
    @Query("SELECT DISTINCT c FROM Contract c " +
           "JOIN ContractParty cp1 ON cp1.contract.id = c.id " +
           "JOIN ContractParty cp2 ON cp2.contract.id = c.id " +
           "JOIN ContractParty cp3 ON cp3.contract.id = c.id " +
           "WHERE cp1.user.id = :userId1 " +
           "AND cp2.user.id = :userId2 " +
           "AND cp3.user.id = :userId3 " +
           "AND c.status = 'ACTIVE' " +
           "AND c.expiryDate > CURRENT_DATE " +
           "ORDER BY c.expiryDate DESC")
    List<Contract> findActiveContractsByThreeParties(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("userId3") Long userId3);
}
