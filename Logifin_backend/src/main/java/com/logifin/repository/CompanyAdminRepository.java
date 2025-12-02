package com.logifin.repository;

import com.logifin.entity.CompanyAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CompanyAdmin entity.
 * Handles company admin/owner data access operations.
 */
@Repository
public interface CompanyAdminRepository extends JpaRepository<CompanyAdmin, Long> {

    /**
     * Find company admin by company ID.
     * @param companyId the company ID
     * @return Optional containing the company admin if found
     */
    @Query("SELECT ca FROM CompanyAdmin ca WHERE ca.company.id = :companyId")
    Optional<CompanyAdmin> findByCompanyId(@Param("companyId") Long companyId);

    /**
     * Check if a company admin exists for the given company.
     * @param companyId the company ID
     * @return true if admin exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CompanyAdmin ca WHERE ca.company.id = :companyId")
    boolean existsByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find company admin by user ID.
     * @param userId the user ID
     * @return Optional containing the company admin if found
     */
    @Query("SELECT ca FROM CompanyAdmin ca WHERE ca.user.id = :userId")
    Optional<CompanyAdmin> findByUserId(@Param("userId") Long userId);

    /**
     * Check if a user is a company admin.
     * @param userId the user ID
     * @return true if user is an admin, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CompanyAdmin ca WHERE ca.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    /**
     * Delete company admin by company ID.
     * @param companyId the company ID
     */
    @Query("DELETE FROM CompanyAdmin ca WHERE ca.company.id = :companyId")
    void deleteByCompanyId(@Param("companyId") Long companyId);
}
