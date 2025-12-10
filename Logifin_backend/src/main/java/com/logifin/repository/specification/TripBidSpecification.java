package com.logifin.repository.specification;

import com.logifin.dto.TripBidSearchCriteria;
import com.logifin.entity.TripBid;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic TripBid queries.
 */
public class TripBidSpecification {

    private TripBidSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Build specification from search criteria
     */
    public static Specification<TripBid> buildSpecification(TripBidSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by trip ID
            if (criteria.getTripId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("trip").get("id"), criteria.getTripId()));
            }

            // Filter by lender ID
            if (criteria.getLenderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("lender").get("id"), criteria.getLenderId()));
            }

            // Filter by company ID
            if (criteria.getCompanyId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), criteria.getCompanyId()));
            }

            // Filter by status
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // Filter by min bid amount
            if (criteria.getMinBidAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("bidAmount"), criteria.getMinBidAmount()));
            }

            // Filter by max bid amount
            if (criteria.getMaxBidAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("bidAmount"), criteria.getMaxBidAmount()));
            }

            // Filter by created from date
            if (criteria.getCreatedFrom() != null) {
                LocalDateTime startOfDay = criteria.getCreatedFrom().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
            }

            // Filter by created to date
            if (criteria.getCreatedTo() != null) {
                LocalDateTime endOfDay = criteria.getCreatedTo().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endOfDay));
            }

            // Exclude expired bids if not requested
            if (criteria.getIncludeExpired() == null || !criteria.getIncludeExpired()) {
                Predicate notExpired = criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("expiresAt")),
                    criteriaBuilder.greaterThan(root.get("expiresAt"), LocalDateTime.now())
                );
                Predicate notExpiredStatus = criteriaBuilder.notEqual(root.get("status"), TripBid.BidStatus.EXPIRED);
                predicates.add(criteriaBuilder.and(notExpired, notExpiredStatus));
            }

            // Keyword search
            if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate lenderNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.concat(
                            criteriaBuilder.concat(root.get("lender").get("firstName"), " "),
                            root.get("lender").get("lastName")
                        )
                    ),
                    keyword
                );
                Predicate companyNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("company").get("name")),
                    keyword
                );
                Predicate notesPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("notes")),
                    keyword
                );
                predicates.add(criteriaBuilder.or(lenderNamePredicate, companyNamePredicate, notesPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Find active bids for a trip (PENDING or COUNTERED)
     */
    public static Specification<TripBid> activeBidsForTrip(Long tripId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("trip").get("id"), tripId));
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.equal(root.get("status"), TripBid.BidStatus.PENDING),
                criteriaBuilder.equal(root.get("status"), TripBid.BidStatus.COUNTERED)
            ));
            // Exclude expired
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("expiresAt")),
                criteriaBuilder.greaterThan(root.get("expiresAt"), LocalDateTime.now())
            ));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Find bids by lender with status filter
     */
    public static Specification<TripBid> bidsByLenderAndStatus(Long lenderId, TripBid.BidStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("lender").get("id"), lenderId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
