package com.logifin.repository.specification;

import com.logifin.dto.TripSearchCriteria;
import com.logifin.entity.Trip;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for Trip entity.
 * Enables dynamic query building based on search criteria.
 */
public class TripSpecification {

    private TripSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Build a Specification from TripSearchCriteria
     */
    public static Specification<Trip> fromCriteria(TripSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Pickup location (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getPickup())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("pickup")),
                        "%" + criteria.getPickup().toLowerCase() + "%"
                ));
            }

            // Destination (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getDestination())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("destination")),
                        "%" + criteria.getDestination().toLowerCase() + "%"
                ));
            }

            // Sender (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getSender())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sender")),
                        "%" + criteria.getSender().toLowerCase() + "%"
                ));
            }

            // Receiver (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getReceiver())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("receiver")),
                        "%" + criteria.getReceiver().toLowerCase() + "%"
                ));
            }

            // Transporter (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getTransporter())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("transporter")),
                        "%" + criteria.getTransporter().toLowerCase() + "%"
                ));
            }

            // Status (exact match)
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // Loan amount range
            if (criteria.getMinLoanAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("loanAmount"), criteria.getMinLoanAmount()
                ));
            }
            if (criteria.getMaxLoanAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("loanAmount"), criteria.getMaxLoanAmount()
                ));
            }

            // Interest rate range
            if (criteria.getMinInterestRate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("interestRate"), criteria.getMinInterestRate()
                ));
            }
            if (criteria.getMaxInterestRate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("interestRate"), criteria.getMaxInterestRate()
                ));
            }

            // Maturity days range
            if (criteria.getMinMaturityDays() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("maturityDays"), criteria.getMinMaturityDays()
                ));
            }
            if (criteria.getMaxMaturityDays() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("maturityDays"), criteria.getMaxMaturityDays()
                ));
            }

            // Load type (partial match, case-insensitive)
            if (StringUtils.hasText(criteria.getLoadType())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("loadType")),
                        "%" + criteria.getLoadType().toLowerCase() + "%"
                ));
            }

            // Created date range
            if (criteria.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.getCreatedFrom().atStartOfDay()
                ));
            }
            if (criteria.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.getCreatedTo().atTime(LocalTime.MAX)
                ));
            }

            // Company ID (exact match)
            if (criteria.getCompanyId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("company").get("id"), criteria.getCompanyId()
                ));
            }

            // Created by user ID (exact match)
            if (criteria.getCreatedByUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("createdByUser").get("id"), criteria.getCreatedByUserId()
                ));
            }

            // Keyword search across multiple fields
            if (StringUtils.hasText(criteria.getKeyword())) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate keywordPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("pickup")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("destination")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sender")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("receiver")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transporter")), keyword)
                );
                predicates.add(keywordPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create specification for status
     */
    public static Specification<Trip> hasStatus(Trip.TripStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Create specification for transporter
     */
    public static Specification<Trip> hasTransporter(String transporter) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(transporter)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("transporter")),
                    "%" + transporter.toLowerCase() + "%"
            );
        };
    }

    /**
     * Create specification for company
     */
    public static Specification<Trip> belongsToCompany(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("company").get("id"), companyId);
        };
    }
}
