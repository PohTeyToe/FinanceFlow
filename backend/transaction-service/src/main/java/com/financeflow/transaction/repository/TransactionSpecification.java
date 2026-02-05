package com.financeflow.transaction.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.financeflow.transaction.model.Transaction;

import jakarta.persistence.criteria.Predicate;

/**
 * JPA Specifications for building dynamic Transaction queries.
 * This approach avoids the PostgreSQL parameter type inference issues
 * that occur with native queries using CAST and IS NULL.
 */
public class TransactionSpecification {

    /**
     * Build a specification for filtering transactions by account and optional criteria.
     * 
     * @param accountId Required account ID
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param type Optional transaction type filter
     * @param category Optional category filter
     * @return Specification for filtering transactions
     */
    public static Specification<Transaction> withFilters(
            UUID accountId,
            Instant startDate,
            Instant endDate,
            String type,
            String category
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Account ID is required
            predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));

            // Optional start date filter
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            // Optional end date filter
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            // Optional transaction type filter
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType").as(String.class), type));
            }

            // Optional category filter
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Add ordering by created date descending
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
