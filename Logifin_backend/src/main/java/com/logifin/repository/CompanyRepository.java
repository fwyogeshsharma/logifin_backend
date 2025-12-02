package com.logifin.repository;

import com.logifin.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmail(String email);

    Optional<Company> findByGstNumber(String gstNumber);

    Optional<Company> findByPanNumber(String panNumber);

    Optional<Company> findByCompanyRegistrationNumber(String companyRegistrationNumber);

    boolean existsByEmail(String email);

    boolean existsByGstNumber(String gstNumber);

    boolean existsByPanNumber(String panNumber);

    boolean existsByCompanyRegistrationNumber(String companyRegistrationNumber);

    List<Company> findByIsActiveTrue();

    List<Company> findByIsVerifiedTrue();

    List<Company> findByIsVerifiedFalse();

    // Paginated queries
    Page<Company> findByIsActiveTrue(Pageable pageable);

    Page<Company> findByIsActiveFalse(Pageable pageable);

    Page<Company> findByIsVerifiedTrue(Pageable pageable);

    Page<Company> findByIsVerifiedFalse(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.state) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Company> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.city = :city")
    Page<Company> findByCity(@Param("city") String city, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.state = :state")
    Page<Company> findByState(@Param("state") String state, Pageable pageable);
}
