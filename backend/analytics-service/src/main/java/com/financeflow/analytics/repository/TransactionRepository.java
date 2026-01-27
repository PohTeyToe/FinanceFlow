package com.financeflow.analytics.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeflow.analytics.model.Transaction;

/**
 * Read-only repository for transaction analytics queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Get spending grouped by category for a specific account
     * Returns: category, totalAmount, transactionCount
     */
    @Query("""
        SELECT t.category, SUM(t.amount), COUNT(t) 
        FROM Transaction t 
        WHERE t.accountId = :accountId
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT')
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY t.category
        ORDER BY SUM(t.amount) DESC
        """)
    List<Object[]> getSpendingByCategoryForAccount(
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get spending grouped by category for all user accounts
     * Returns: category, totalAmount, transactionCount
     */
    @Query("""
        SELECT t.category, SUM(t.amount), COUNT(t) 
        FROM Transaction t 
        JOIN Account a ON t.accountId = a.id
        WHERE a.userId = :userId
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT')
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY t.category
        ORDER BY SUM(t.amount) DESC
        """)
    List<Object[]> getSpendingByCategoryForUser(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get total spending for an account within date range
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM Transaction t 
        WHERE t.accountId = :accountId
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT')
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal getTotalSpendingForAccount(
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get total spending for all user accounts within date range
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM Transaction t 
        JOIN Account a ON t.accountId = a.id
        WHERE a.userId = :userId
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT')
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal getTotalSpendingForUser(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get total income for all user accounts within date range
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM Transaction t 
        JOIN Account a ON t.accountId = a.id
        WHERE a.userId = :userId
        AND t.transactionType IN ('DEPOSIT', 'TRANSFER_IN')
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal getTotalIncomeForUser(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get monthly income and expense totals for user
     * Returns: year, month, income, expenses
     */
    @Query(value = """
        SELECT 
            EXTRACT(YEAR FROM t.created_at) as year,
            EXTRACT(MONTH FROM t.created_at) as month,
            COALESCE(SUM(CASE WHEN t.transaction_type IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount ELSE 0 END), 0) as income,
            COALESCE(SUM(CASE WHEN t.transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN t.amount ELSE 0 END), 0) as expenses
        FROM transactions t
        JOIN accounts a ON t.account_id = a.id
        WHERE a.user_id = :userId
        AND t.status = 'COMPLETED'
        AND t.created_at >= :startDate
        GROUP BY EXTRACT(YEAR FROM t.created_at), EXTRACT(MONTH FROM t.created_at)
        ORDER BY year DESC, month DESC
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrendForUser(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate
    );

    /**
     * Get monthly income and expense totals for specific account
     * Returns: year, month, income, expenses
     */
    @Query(value = """
        SELECT 
            EXTRACT(YEAR FROM t.created_at) as year,
            EXTRACT(MONTH FROM t.created_at) as month,
            COALESCE(SUM(CASE WHEN t.transaction_type IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount ELSE 0 END), 0) as income,
            COALESCE(SUM(CASE WHEN t.transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN t.amount ELSE 0 END), 0) as expenses
        FROM transactions t
        WHERE t.account_id = :accountId
        AND t.status = 'COMPLETED'
        AND t.created_at >= :startDate
        GROUP BY EXTRACT(YEAR FROM t.created_at), EXTRACT(MONTH FROM t.created_at)
        ORDER BY year DESC, month DESC
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrendForAccount(
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate
    );

    /**
     * Get top spending category for user within date range
     */
    @Query("""
        SELECT t.category 
        FROM Transaction t 
        JOIN Account a ON t.accountId = a.id
        WHERE a.userId = :userId
        AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT')
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        AND t.category IS NOT NULL
        GROUP BY t.category
        ORDER BY SUM(t.amount) DESC
        LIMIT 1
        """)
    String getTopSpendingCategoryForUser(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get income vs expenses for specific account within date range
     * Returns: income, expenses
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN t.transactionType IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN t.amount ELSE 0 END), 0)
        FROM Transaction t 
        WHERE t.accountId = :accountId
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        """)
    Object[] getIncomeVsExpensesForAccount(
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Get income vs expenses for all user accounts within date range
     * Returns: income, expenses
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN t.transactionType IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN t.amount ELSE 0 END), 0)
        FROM Transaction t 
        JOIN Account a ON t.accountId = a.id
        WHERE a.userId = :userId
        AND t.status = 'COMPLETED'
        AND t.createdAt BETWEEN :startDate AND :endDate
        """)
    Object[] getIncomeVsExpensesForUser(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
