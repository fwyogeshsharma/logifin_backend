package com.logifin.repository;

import com.logifin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    // Paginated queries
    Page<User> findByActiveTrue(Pageable pageable);

    Page<User> findByActiveFalse(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.active = :active ORDER BY u.createdAt DESC")
    List<User> findAllByActive(@Param("active") Boolean active);

    @Query("SELECT u FROM User u WHERE u.active = :active")
    Page<User> findAllByActive(@Param("active") Boolean active, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> searchByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<User> findByRole_RoleName(String roleName, Pageable pageable);

    /**
     * Check if any user exists for the given company.
     * Used to determine if a new user should become company admin.
     * @param companyId the company ID
     * @return true if at least one user exists for the company
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.company.id = :companyId")
    boolean existsByCompanyId(@Param("companyId") Long companyId);

    /**
     * Count users in a company.
     * @param companyId the company ID
     * @return number of users in the company
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find all users by company ID.
     * @param companyId the company ID
     * @param pageable pagination info
     * @return page of users in the company
     */
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId")
    Page<User> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
