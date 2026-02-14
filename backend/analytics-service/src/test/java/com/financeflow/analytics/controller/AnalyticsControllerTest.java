package com.financeflow.analytics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.financeflow.analytics.config.JwtConfig;
import com.financeflow.analytics.config.SecurityConfig;
import com.financeflow.analytics.dto.AccountInfo;
import com.financeflow.analytics.dto.AccountSummaryResponse;
import com.financeflow.analytics.dto.CategorySpending;
import com.financeflow.analytics.dto.IncomeVsExpensesResponse;
import com.financeflow.analytics.dto.MonthlyData;
import com.financeflow.analytics.dto.MonthlyTrendResponse;
import com.financeflow.analytics.dto.SpendingByCategoryResponse;
import com.financeflow.analytics.service.AnalyticsService;
import com.financeflow.analytics.service.JwtService;

@WebMvcTest(AnalyticsController.class)
@Import({SecurityConfig.class, JwtConfig.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtService jwtService;

    private UUID userId;
    private UUID accountId;
    private String validToken;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        accountId = UUID.randomUUID();
        validToken = "valid-jwt-token";

        // Setup JWT mocks
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractUserId(validToken)).thenReturn(userId);
        when(jwtService.extractEmail(validToken)).thenReturn("test@example.com");
    }

    @Test
    @DisplayName("should return 401 when no authentication")
    void shouldReturn401WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/analytics/spending-by-category"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return spending by category")
    void shouldReturnSpendingByCategory() throws Exception {
        // Arrange
        List<CategorySpending> categories = Arrays.asList(
                CategorySpending.builder()
                        .category("Food & Dining")
                        .amount(new BigDecimal("500.00"))
                        .percentage(new BigDecimal("71.43"))
                        .transactionCount(10L)
                        .build(),
                CategorySpending.builder()
                        .category("Transportation")
                        .amount(new BigDecimal("200.00"))
                        .percentage(new BigDecimal("28.57"))
                        .transactionCount(5L)
                        .build()
        );

        SpendingByCategoryResponse response = SpendingByCategoryResponse.builder()
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .totalSpending(new BigDecimal("700.00"))
                .categories(categories)
                .build();

        when(analyticsService.getSpendingByCategory(eq(userId), any(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/spending-by-category")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpending").value(700.00))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].category").value("Food & Dining"))
                .andExpect(jsonPath("$.categories[0].amount").value(500.00));
    }

    @Test
    @DisplayName("should return spending by category with query params")
    void shouldReturnSpendingByCategoryWithQueryParams() throws Exception {
        // Arrange
        SpendingByCategoryResponse response = SpendingByCategoryResponse.builder()
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .totalSpending(new BigDecimal("1000.00"))
                .categories(Collections.emptyList())
                .build();

        when(analyticsService.getSpendingByCategory(eq(userId), eq(accountId), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/spending-by-category")
                        .header("Authorization", "Bearer " + validToken)
                        .param("accountId", accountId.toString())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpending").value(1000.00));
    }

    @Test
    @DisplayName("should return monthly trend")
    void shouldReturnMonthlyTrend() throws Exception {
        // Arrange
        List<MonthlyData> months = Arrays.asList(
                MonthlyData.builder()
                        .month("2024-01")
                        .income(new BigDecimal("3500.00"))
                        .expenses(new BigDecimal("2100.00"))
                        .netSavings(new BigDecimal("1400.00"))
                        .build()
        );

        MonthlyTrendResponse response = MonthlyTrendResponse.builder()
                .numberOfMonths(6)
                .months(months)
                .build();

        when(analyticsService.getMonthlyTrend(eq(userId), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/monthly-trend")
                        .header("Authorization", "Bearer " + validToken)
                        .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfMonths").value(6))
                .andExpect(jsonPath("$.months").isArray())
                .andExpect(jsonPath("$.months[0].month").value("2024-01"))
                .andExpect(jsonPath("$.months[0].income").value(3500.00));
    }

    @Test
    @DisplayName("should return account summary")
    void shouldReturnAccountSummary() throws Exception {
        // Arrange
        List<AccountInfo> accounts = List.of(
                AccountInfo.builder()
                        .accountId(accountId)
                        .accountName("Primary Checking")
                        .balance(new BigDecimal("5000.00"))
                        .accountType("CHECKING")
                        .build()
        );

        AccountSummaryResponse response = AccountSummaryResponse.builder()
                .totalBalance(new BigDecimal("5000.00"))
                .accounts(accounts)
                .thisMonthSpending(new BigDecimal("1200.00"))
                .thisMonthIncome(new BigDecimal("3500.00"))
                .topSpendingCategory("Food & Dining")
                .comparedToLastMonth(new BigDecimal("-15.50"))
                .build();

        when(analyticsService.getAccountSummary(userId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/summary")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance").value(5000.00))
                .andExpect(jsonPath("$.accounts[0].accountName").value("Primary Checking"))
                .andExpect(jsonPath("$.thisMonthSpending").value(1200.00))
                .andExpect(jsonPath("$.topSpendingCategory").value("Food & Dining"))
                .andExpect(jsonPath("$.comparedToLastMonth").value(-15.50));
    }

    @Test
    @DisplayName("should return income vs expenses")
    void shouldReturnIncomeVsExpenses() throws Exception {
        // Arrange
        IncomeVsExpensesResponse response = IncomeVsExpensesResponse.builder()
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .totalIncome(new BigDecimal("3500.00"))
                .totalExpenses(new BigDecimal("2100.00"))
                .netSavings(new BigDecimal("1400.00"))
                .savingsRate(new BigDecimal("40.00"))
                .build();

        when(analyticsService.getIncomeVsExpenses(eq(userId), any(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/income-vs-expenses")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3500.00))
                .andExpect(jsonPath("$.totalExpenses").value(2100.00))
                .andExpect(jsonPath("$.netSavings").value(1400.00))
                .andExpect(jsonPath("$.savingsRate").value(40.00));
    }

    @Test
    @DisplayName("should handle invalid date format")
    void shouldHandleInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/analytics/spending-by-category")
                        .header("Authorization", "Bearer " + validToken)
                        .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}
