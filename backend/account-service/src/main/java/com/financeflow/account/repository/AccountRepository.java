package com.financeflow.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeflow.account.model.Account;
import com.financeflow.account.model.AccountType;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find all accounts for a specific user
     */
    List<Account> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find all accounts for a user (including inactive)
     */
    List<Account> findByUserId(UUID userId);

    /**
     * Find account by ID if it belongs to the specified user
     */
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Count accounts of a specific type for a user (for account number generation)
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.userId = :userId AND a.accountType = :accountType")
    long countByUserIdAndAccountType(@Param("userId") UUID userId, @Param("accountType") AccountType accountType);

    /**
     * Check if account number already exists
     */
    boolean existsByAccountNumber(String accountNumber);
}
