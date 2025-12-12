package com.logifin.repository.specification;

import com.logifin.dto.ContractSearchCriteria;
import com.logifin.entity.Contract;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for Contract entity.
 * Enables dynamic query building based on search criteria.
 */
public class ContractSpecification {

    private ContractSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Build a Specification from ContractSearchCriteria
     */
    public static Specification<Contract> fromCriteria(ContractSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Contract number (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getContractNumber())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("contractNumber")),
                        "%" + criteria.getContractNumber().toLowerCase() + "%"
                ));
            }

            // Contract type ID (exact match)
            if (criteria.getContractTypeId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("contractType").get("id"), criteria.getContractTypeId()
                ));
            }

            // Contract manager ID (exact match)
            if (criteria.getContractManagerId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("contractManager").get("id"), criteria.getContractManagerId()
                ));
            }

            // Consigner company ID (exact match)
            if (criteria.getConsignerCompanyId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("consignerCompany").get("id"), criteria.getConsignerCompanyId()
                ));
            }

            // Loan stage ID (exact match)
            if (criteria.getLoanStageId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("loanStage").get("id"), criteria.getLoanStageId()
                ));
            }

            // Status (exact match)
            if (StringUtils.hasText(criteria.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // Loan percent range
            if (criteria.getMinLoanPercent() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("loanPercent"), criteria.getMinLoanPercent()
                ));
            }
            if (criteria.getMaxLoanPercent() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("loanPercent"), criteria.getMaxLoanPercent()
                ));
            }

            // LTV range
            if (criteria.getMinLtv() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("ltv"), criteria.getMinLtv()
                ));
            }
            if (criteria.getMaxLtv() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("ltv"), criteria.getMaxLtv()
                ));
            }

            // Penalty ratio range
            if (criteria.getMinPenaltyRatio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("penaltyRatio"), criteria.getMinPenaltyRatio()
                ));
            }
            if (criteria.getMaxPenaltyRatio() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("penaltyRatio"), criteria.getMaxPenaltyRatio()
                ));
            }

            // Expiry date range
            if (criteria.getExpiryDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("expiryDate"), criteria.getExpiryDateFrom()
                ));
            }
            if (criteria.getExpiryDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("expiryDate"), criteria.getExpiryDateTo()
                ));
            }

            // Created date range
            if (criteria.getCreatedDateFrom() != null) {
                LocalDateTime startDateTime = criteria.getCreatedDateFrom().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), startDateTime
                ));
            }
            if (criteria.getCreatedDateTo() != null) {
                LocalDateTime endDateTime = criteria.getCreatedDateTo().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), endDateTime
                ));
            }

            // Party user ID (contracts where user is a party)
            if (criteria.getPartyUserId() != null) {
                Join<Object, Object> partiesJoin = root.join("contractParties", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(
                        partiesJoin.get("user").get("id"), criteria.getPartyUserId()
                ));
            }

            // Created by user ID (exact match)
            if (criteria.getCreatedByUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("createdBy").get("id"), criteria.getCreatedByUserId()
                ));
            }

            // Keyword search across multiple fields
            if (StringUtils.hasText(criteria.getKeyword())) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";

                // Join with related entities for keyword search
                Join<Object, Object> managerJoin = root.join("contractManager", JoinType.LEFT);
                Join<Object, Object> companyJoin = root.join("consignerCompany", JoinType.LEFT);

                Predicate keywordPredicate = criteriaBuilder.or(
                        // Contract number
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("contractNumber")), keyword),
                        // Manager first name
                        criteriaBuilder.like(criteriaBuilder.lower(managerJoin.get("firstName")), keyword),
                        // Manager last name
                        criteriaBuilder.like(criteriaBuilder.lower(managerJoin.get("lastName")), keyword),
                        // Company name
                        criteriaBuilder.like(criteriaBuilder.lower(companyJoin.get("name")), keyword)
                );
                predicates.add(keywordPredicate);
            }

            // Avoid duplicate results when using joins
            if (query != null) {
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create specification for status
     */
    public static Specification<Contract> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(status)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Create specification for contract type
     */
    public static Specification<Contract> hasContractType(Long contractTypeId) {
        return (root, query, criteriaBuilder) -> {
            if (contractTypeId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("contractType").get("id"), contractTypeId);
        };
    }

    /**
     * Create specification for consigner company
     */
    public static Specification<Contract> belongsToCompany(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("consignerCompany").get("id"), companyId);
        };
    }

    /**
     * Create specification for contract manager
     */
    public static Specification<Contract> managedBy(Long managerId) {
        return (root, query, criteriaBuilder) -> {
            if (managerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("contractManager").get("id"), managerId);
        };
    }

    /**
     * Create specification for loan stage
     */
    public static Specification<Contract> hasLoanStage(Long loanStageId) {
        return (root, query, criteriaBuilder) -> {
            if (loanStageId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("loanStage").get("id"), loanStageId);
        };
    }

    /**
     * Create specification for expired contracts
     */
    public static Specification<Contract> isExpired() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThan(root.get("expiryDate"), java.time.LocalDate.now());
    }

    /**
     * Create specification for active contracts
     */
    public static Specification<Contract> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.equal(root.get("status"), "ACTIVE"),
            criteriaBuilder.greaterThanOrEqualTo(root.get("expiryDate"), java.time.LocalDate.now())
        );
    }
}
