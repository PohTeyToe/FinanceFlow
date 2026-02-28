package com.financeflow.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeflow.analytics.dto.AccountSummaryResponse;
import com.financeflow.analytics.dto.IncomeVsExpensesResponse;
import com.financeflow.analytics.dto.MonthlyTrendResponse;
import com.financeflow.analytics.dto.SpendingByCategoryResponse;
import com.financeflow.analytics.exception.UnauthorizedAccessException;
import com.financeflow.analytics.model.Account;
import com.financeflow.analytics.model.AccountType;
import com.financeflow.analytics.repository.AccountRepository;
import com.financeflow.analytics.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private UUID userId;
    private UUID accountId;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        testAccount = Account.builder()
                .id(accountId)
                .userId(userId)
                .accountNumber("CHK-001")
                .accountType(AccountType.CHECKING)
                .accountName("Primary Checking")
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("getSpendingByCategory")
    class GetSpendingByCategory {

        @Test
        @DisplayName("should return spending by category for all user accounts")
        void shouldReturnSpendingByCategoryForAllUserAccounts() {
            // Arrange
            List<Object[]> mockResults = Arrays.asList(
                    new Object[]{"Food & Dining", new BigDecimal("500.00"), 10L},
                    new Object[]{"Transportation", new BigDecimal("200.00"), 5L}
            );
            when(transactionRepository.getSpendingByCategoryForUser(eq(userId), any(), any()))
                    .thenReturn(mockResults);
            when(transactionRepository.getTotalSpendingForUser(eq(userId), any(), any()))
                    .thenReturn(new BigDecimal("700.00"));

            // Act
            SpendingByCategoryResponse response = analyticsService.getSpendingByCategory(
                    userId, null, null, null);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalSpending()).isEqualByComparingTo("700.00");
            assertThat(response.getCategories()).hasSize(2);
            assertThat(response.getCategories().get(0).getCategory()).isEqualTo("Food & Dining");
            assertThat(response.getCategories().get(0).getAmount()).isEqualByComparingTo("500.00");
            assertThat(response.getCategories().get(0).getTransactionCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("should return spending by category for specific account")
        void shouldReturnSpendingByCategoryForSpecificAccount() {
            // Arrange
            when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
            List<Object[]> mockResults = Collections.singletonList(
                    new Object[]{"Shopping", new BigDecimal("300.00"), 3L}
            );
            when(transactionRepository.getSpendingByCategoryForAccount(eq(accountId), any(), any()))
                    .thenReturn(mockResults);
            when(transactionRepository.getTotalSpendingForAccount(eq(accountId), any(), any()))
                    .thenReturn(new BigDecimal("300.00"));

            // Act
            SpendingByCategoryResponse response = analyticsService.getSpendingByCategory(
                    userId, accountId, LocalDate.now().minusDays(30), LocalDate.now());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalSpending()).isEqualByComparingTo("300.00");
            assertThat(response.getCategories()).hasSize(1);
        }

        @Test
        @DisplayName("should throw exception when account does not belong to user")
        void shouldThrowExceptionWhenAccountDoesNotBelongToUser() {
            // Arrange
            when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> analyticsService.getSpendingByCategory(
                    userId, accountId, null, null))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("should handle empty results")
        void shouldHandleEmptyResults() {
            // Arrange
            when(transactionRepository.getSpendingByCategoryForUser(eq(userId), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(transactionRepository.getTotalSpendingForUser(eq(userId), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            // Act
            SpendingByCategoryResponse response = analyticsService.getSpendingByCategory(
                    userId, null, null, null);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalSpending()).isEqualByComparingTo("0");
            assertThat(response.getCategories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMonthlyTrend")
    class GetMonthlyTrend {

        @Test
        @DisplayName("should return monthly trend for user")
        void shouldReturnMonthlyTrendForUser() {
            // Arrange
            List<Object[]> mockResults = Arrays.asList(
                    new Object[]{2024, 1, new BigDecimal("3500.00"), new BigDecimal("2100.00")},
                    new Object[]{2023, 12, new BigDecimal("3500.00"), new BigDecimal("2800.00")}
            );
            when(transactionRepository.getMonthlyTrendForUser(eq(userId), any()))
                    .thenReturn(mockResults);

            // Act
            MonthlyTrendResponse response = analyticsService.getMonthlyTrend(userId, null, 6);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getNumberOfMonths()).isEqualTo(6);
            assertThat(response.getMonths()).hasSize(2);
            assertThat(response.getMonths().get(0).getMonth()).isEqualTo("2024-01");
            assertThat(response.getMonths().get(0).getIncome()).isEqualByComparingTo("3500.00");
            assertThat(response.getMonths().get(0).getExpenses()).isEqualByComparingTo("2100.00");
            assertThat(response.getMonths().get(0).getNetSavings()).isEqualByComparingTo("1400.00");
        }

        @Test
        @DisplayName("should default to 6 months when null")
        void shouldDefaultTo6MonthsWhenNull() {
            // Arrange
            when(transactionRepository.getMonthlyTrendForUser(eq(userId), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            MonthlyTrendResponse response = analyticsService.getMonthlyTrend(userId, null, null);

            // Assert
            assertThat(response.getNumberOfMonths()).isEqualTo(6);
        }

        @Test
        @DisplayName("should cap months at 24")
        void shouldCapMonthsAt24() {
            // Arrange
            when(transactionRepository.getMonthlyTrendForUser(eq(userId), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            MonthlyTrendResponse response = analyticsService.getMonthlyTrend(userId, null, 100);

            // Assert
            assertThat(response.getNumberOfMonths()).isEqualTo(24);
        }
    }

    @Nested
    @DisplayName("getAccountSummary")
    class GetAccountSummary {

        @Test
        @DisplayName("should return account summary with insights")
        void shouldReturnAccountSummaryWithInsights() {
            // Arrange
            when(accountRepository.findByUserIdAndIsActiveTrue(userId))
                    .thenReturn(List.of(testAccount));
            when(accountRepository.getTotalBalanceByUserId(userId))
                    .thenReturn(new BigDecimal("5000.00"));
            when(transactionRepository.getTotalSpendingForUser(eq(userId), any(), any()))
                    .thenReturn(new BigDecimal("1200.00"))  // this month
                    .thenReturn(new BigDecimal("1000.00")); // last month
            when(transactionRepository.getTotalIncomeForUser(eq(userId), any(), any()))
                    .thenReturn(new BigDecimal("3500.00"));
            when(transactionRepository.getTopSpendingCategoryForUser(eq(userId), any(), any()))
                    .thenReturn("Food & Dining");

            // Act
            AccountSummaryResponse response = analyticsService.getAccountSummary(userId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalBalance()).isEqualByComparingTo("5000.00");
            assertThat(response.getAccounts()).hasSize(1);
            assertThat(response.getThisMonthSpending()).isEqualByComparingTo("1200.00");
            assertThat(response.getThisMonthIncome()).isEqualByComparingTo("3500.00");
            assertThat(response.getTopSpendingCategory()).isEqualTo("Food & Dining");
            assertThat(response.getComparedToLastMonth()).isEqualByComparingTo("20.00"); // 20% increase
        }

        @Test
        @DisplayName("should handle user with no accounts")
        void shouldHandleUserWithNoAccounts() {
            // Arrange
            when(accountRepository.findByUserIdAndIsActiveTrue(userId))
                    .thenReturn(Collections.emptyList());
            when(accountRepository.getTotalBalanceByUserId(userId))
                    .thenReturn(BigDecimal.ZERO);
            when(transactionRepository.getTotalSpendingForUser(eq(userId), any(), any()))
                    .thenReturn(BigDecimal.ZERO);
            when(transactionRepository.getTotalIncomeForUser(eq(userId), any(), any()))
                    .thenReturn(BigDecimal.ZERO);
            when(transactionRepository.getTopSpendingCategoryForUser(eq(userId), any(), any()))
                    .thenReturn(null);

            // Act
            AccountSummaryResponse response = analyticsService.getAccountSummary(userId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalBalance()).isEqualByComparingTo("0");
            assertThat(response.getAccounts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getIncomeVsExpenses")
    class GetIncomeVsExpenses {

        @Test
        @DisplayName("should return income vs expenses for user")
        void shouldReturnIncomeVsExpensesForUser() {
            // Arrange
            Object[] mockResult = new Object[]{new BigDecimal("3500.00"), new BigDecimal("2100.00")};
            when(transactionRepository.getIncomeVsExpensesForUser(eq(userId), any(), any()))
                    .thenReturn(mockResult);

            // Act
            IncomeVsExpensesResponse response = analyticsService.getIncomeVsExpenses(
                    userId, null, null, null);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalIncome()).isEqualByComparingTo("3500.00");
            assertThat(response.getTotalExpenses()).isEqualByComparingTo("2100.00");
            assertThat(response.getNetSavings()).isEqualByComparingTo("1400.00");
            assertThat(response.getSavingsRate()).isEqualByComparingTo("40.00"); // 40% savings rate
        }

        @Test
        @DisplayName("should handle zero income")
        void shouldHandleZeroIncome() {
            // Arrange
            Object[] mockResult = new Object[]{BigDecimal.ZERO, new BigDecimal("100.00")};
            when(transactionRepository.getIncomeVsExpensesForUser(eq(userId), any(), any()))
                    .thenReturn(mockResult);

            // Act
            IncomeVsExpensesResponse response = analyticsService.getIncomeVsExpenses(
                    userId, null, null, null);

            // Assert
            assertThat(response.getSavingsRate()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("should return income vs expenses for specific account")
        void shouldReturnIncomeVsExpensesForSpecificAccount() {
            // Arrange
            when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
            Object[] mockResult = new Object[]{new BigDecimal("2000.00"), new BigDecimal("1500.00")};
            when(transactionRepository.getIncomeVsExpensesForAccount(eq(accountId), any(), any()))
                    .thenReturn(mockResult);

            // Act
            IncomeVsExpensesResponse response = analyticsService.getIncomeVsExpenses(
                    userId, accountId, LocalDate.now().minusDays(30), LocalDate.now());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalIncome()).isEqualByComparingTo("2000.00");
            assertThat(response.getTotalExpenses()).isEqualByComparingTo("1500.00");
        }
    }
}
