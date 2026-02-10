package com.financeflow.analytics.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeflow.analytics.model.Account;

/**
 * Read-only repository for account queries.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find all active accounts for a user
     */
    List<Account> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find account by ID and verify ownership
     */
    Optional<Account> findByIdAndUserId(UUID accountId, UUID userId);

    /**
     * Get total balance across all user accounts
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.userId = :userId AND a.isActive = true")
    java.math.BigDecimal getTotalBalanceByUserId(@Param("userId") UUID userId);

    /**
     * Check if account belongs to user
     */
    boolean existsByIdAndUserId(UUID accountId, UUID userId);
}
