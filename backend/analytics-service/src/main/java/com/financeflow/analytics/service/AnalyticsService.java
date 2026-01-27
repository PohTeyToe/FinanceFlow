package com.financeflow.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeflow.analytics.dto.AccountInfo;
import com.financeflow.analytics.dto.AccountSummaryResponse;
import com.financeflow.analytics.dto.CategorySpending;
import com.financeflow.analytics.dto.IncomeVsExpensesResponse;
import com.financeflow.analytics.dto.MonthlyData;
import com.financeflow.analytics.dto.MonthlyTrendResponse;
import com.financeflow.analytics.dto.SpendingByCategoryResponse;
import com.financeflow.analytics.exception.AccountNotFoundException;
import com.financeflow.analytics.exception.UnauthorizedAccessException;
import com.financeflow.analytics.model.Account;
import com.financeflow.analytics.repository.AccountRepository;
import com.financeflow.analytics.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    /**
     * Get spending breakdown by category
     */
    public SpendingByCategoryResponse getSpendingByCategory(
            UUID userId, 
            UUID accountId, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        // Set default dates if not provided
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        Instant startInstant = startDate.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        List<Object[]> results;
        BigDecimal totalSpending;

        if (accountId != null) {
            // Verify account ownership
            verifyAccountOwnership(accountId, userId);
            results = transactionRepository.getSpendingByCategoryForAccount(accountId, startInstant, endInstant);
            totalSpending = transactionRepository.getTotalSpendingForAccount(accountId, startInstant, endInstant);
        } else {
            results = transactionRepository.getSpendingByCategoryForUser(userId, startInstant, endInstant);
            totalSpending = transactionRepository.getTotalSpendingForUser(userId, startInstant, endInstant);
        }

        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }

        List<CategorySpending> categories = new ArrayList<>();
        for (Object[] row : results) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            Long count = (Long) row[2];

            BigDecimal percentage = BigDecimal.ZERO;
            if (totalSpending.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(BigDecimal.valueOf(100))
                        .divide(totalSpending, 2, RoundingMode.HALF_UP);
            }

            categories.add(CategorySpending.builder()
                    .category(category != null ? category : "Uncategorized")
                    .amount(amount)
                    .percentage(percentage)
                    .transactionCount(count)
                    .build());
        }

        return SpendingByCategoryResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalSpending(totalSpending)
                .categories(categories)
                .build();
    }

    /**
     * Get monthly spending trend
     */
    public MonthlyTrendResponse getMonthlyTrend(UUID userId, UUID accountId, Integer months) {
        if (months == null || months < 1) {
            months = 6;
        }
        if (months > 24) {
            months = 24; // Cap at 2 years
        }

        LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        Instant startInstant = startDate.atStartOfDay(DEFAULT_ZONE).toInstant();

        List<Object[]> results;
        if (accountId != null) {
            verifyAccountOwnership(accountId, userId);
            results = transactionRepository.getMonthlyTrendForAccount(accountId, startInstant);
        } else {
            results = transactionRepository.getMonthlyTrendForUser(userId, startInstant);
        }

        List<MonthlyData> monthlyDataList = new ArrayList<>();
        for (Object[] row : results) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal income = toBigDecimal(row[2]);
            BigDecimal expenses = toBigDecimal(row[3]);
            BigDecimal netSavings = income.subtract(expenses);

            String monthStr = String.format("%d-%02d", year, month);

            monthlyDataList.add(MonthlyData.builder()
                    .month(monthStr)
                    .income(income)
                    .expenses(expenses)
                    .netSavings(netSavings)
                    .build());
        }

        return MonthlyTrendResponse.builder()
                .numberOfMonths(months)
                .months(monthlyDataList)
                .build();
    }

    /**
     * Get account summary with insights
     */
    public AccountSummaryResponse getAccountSummary(UUID userId) {
        // Get all user accounts
        List<Account> accounts = accountRepository.findByUserIdAndIsActiveTrue(userId);
        
        List<AccountInfo> accountInfos = accounts.stream()
                .map(a -> AccountInfo.builder()
                        .accountId(a.getId())
                        .accountName(a.getAccountName())
                        .balance(a.getBalance())
                        .accountType(a.getAccountType().name())
                        .build())
                .toList();

        // Calculate total balance
        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(userId);
        if (totalBalance == null) {
            totalBalance = BigDecimal.ZERO;
        }

        // Get this month's data
        YearMonth currentMonth = YearMonth.now();
        LocalDate thisMonthStart = currentMonth.atDay(1);
        LocalDate thisMonthEnd = currentMonth.atEndOfMonth();
        Instant thisMonthStartInstant = thisMonthStart.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant thisMonthEndInstant = thisMonthEnd.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        BigDecimal thisMonthSpending = transactionRepository.getTotalSpendingForUser(
                userId, thisMonthStartInstant, thisMonthEndInstant);
        BigDecimal thisMonthIncome = transactionRepository.getTotalIncomeForUser(
                userId, thisMonthStartInstant, thisMonthEndInstant);

        if (thisMonthSpending == null) thisMonthSpending = BigDecimal.ZERO;
        if (thisMonthIncome == null) thisMonthIncome = BigDecimal.ZERO;

        // Get last month's spending for comparison
        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDate lastMonthStart = lastMonth.atDay(1);
        LocalDate lastMonthEnd = lastMonth.atEndOfMonth();
        Instant lastMonthStartInstant = lastMonthStart.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant lastMonthEndInstant = lastMonthEnd.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        BigDecimal lastMonthSpending = transactionRepository.getTotalSpendingForUser(
                userId, lastMonthStartInstant, lastMonthEndInstant);
        if (lastMonthSpending == null) lastMonthSpending = BigDecimal.ZERO;

        // Calculate percentage change
        BigDecimal comparedToLastMonth = null;
        if (lastMonthSpending.compareTo(BigDecimal.ZERO) > 0) {
            comparedToLastMonth = thisMonthSpending.subtract(lastMonthSpending)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthSpending, 2, RoundingMode.HALF_UP);
        } else if (thisMonthSpending.compareTo(BigDecimal.ZERO) > 0) {
            comparedToLastMonth = BigDecimal.valueOf(100); // 100% increase if last month was 0
        } else {
            comparedToLastMonth = BigDecimal.ZERO;
        }

        // Get top spending category for this month
        String topSpendingCategory = transactionRepository.getTopSpendingCategoryForUser(
                userId, thisMonthStartInstant, thisMonthEndInstant);

        return AccountSummaryResponse.builder()
                .totalBalance(totalBalance)
                .accounts(accountInfos)
                .thisMonthSpending(thisMonthSpending)
                .thisMonthIncome(thisMonthIncome)
                .topSpendingCategory(topSpendingCategory)
                .comparedToLastMonth(comparedToLastMonth)
                .build();
    }

    /**
     * Get income vs expenses comparison
     */
    public IncomeVsExpensesResponse getIncomeVsExpenses(
            UUID userId, 
            UUID accountId, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        // Set default dates if not provided
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        Instant startInstant = startDate.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        Object[] results;
        if (accountId != null) {
            verifyAccountOwnership(accountId, userId);
            results = transactionRepository.getIncomeVsExpensesForAccount(accountId, startInstant, endInstant);
        } else {
            results = transactionRepository.getIncomeVsExpensesForUser(userId, startInstant, endInstant);
        }

        BigDecimal totalIncome = toBigDecimal(results[0]);
        BigDecimal totalExpenses = toBigDecimal(results[1]);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        // Calculate savings rate
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, RoundingMode.HALF_UP);
        }

        return IncomeVsExpensesResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .build();
    }

    /**
     * Verify that the account belongs to the user
     */
    private void verifyAccountOwnership(UUID accountId, UUID userId) {
        if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
            log.warn("User {} attempted to access account {} which they don't own", userId, accountId);
            throw new UnauthorizedAccessException("Account not found or access denied");
        }
    }

    /**
     * Safely convert Object to BigDecimal
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
