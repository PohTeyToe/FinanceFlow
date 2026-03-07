package com.financeflow.transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.financeflow.transaction.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    /**
     * Find all transactions for an account (paginated)
     */
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    /**
     * Check if reference number exists
     */
    boolean existsByReferenceNumber(String referenceNumber);

    /**
     * Count transactions for an account
     */
    long countByAccountId(UUID accountId);

    /**
     * Find transaction by idempotency key to prevent duplicate processing
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
