package com.financeflow.transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeflow.transaction.model.Account;

import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find account by ID if it belongs to the specified user
     */
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Find account by ID (regardless of user) - for receiving transfers
     */
    Optional<Account> findByIdAndIsActiveTrue(UUID id);

    /**
     * Find account by ID with pessimistic lock for balance updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.isActive = true")
    Optional<Account> findByIdWithLock(@Param("id") UUID id);

    /**
     * Find account by ID and user with pessimistic lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.userId = :userId AND a.isActive = true")
    Optional<Account> findByIdAndUserIdWithLock(@Param("id") UUID id, @Param("userId") UUID userId);
}
