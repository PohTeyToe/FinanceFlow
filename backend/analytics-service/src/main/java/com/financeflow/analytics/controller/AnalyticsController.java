package com.financeflow.analytics.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeflow.analytics.config.JwtAuthFilter.AuthenticatedUser;
import com.financeflow.analytics.dto.AccountSummaryResponse;
import com.financeflow.analytics.dto.IncomeVsExpensesResponse;
import com.financeflow.analytics.dto.MonthlyTrendResponse;
import com.financeflow.analytics.dto.SpendingByCategoryResponse;
import com.financeflow.analytics.service.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Spending insights, trends, and financial summaries")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spending-by-category")
    public ResponseEntity<SpendingByCategoryResponse> getSpendingByCategory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting spending by category for user {}, accountId: {}, startDate: {}, endDate: {}", 
                user.userId(), accountId, startDate, endDate);
        
        SpendingByCategoryResponse response = analyticsService.getSpendingByCategory(
                user.userId(), accountId, startDate, endDate);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<MonthlyTrendResponse> getMonthlyTrend(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        
        log.debug("Getting monthly trend for user {}, accountId: {}, months: {}", 
                user.userId(), accountId, months);
        
        MonthlyTrendResponse response = analyticsService.getMonthlyTrend(
                user.userId(), accountId, months);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary(
            @AuthenticationPrincipal AuthenticatedUser user) {
        
        log.debug("Getting account summary for user {}", user.userId());
        
        AccountSummaryResponse response = analyticsService.getAccountSummary(user.userId());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/income-vs-expenses")
    public ResponseEntity<IncomeVsExpensesResponse> getIncomeVsExpenses(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting income vs expenses for user {}, accountId: {}, startDate: {}, endDate: {}", 
                user.userId(), accountId, startDate, endDate);
        
        IncomeVsExpensesResponse response = analyticsService.getIncomeVsExpenses(
                user.userId(), accountId, startDate, endDate);
        
        return ResponseEntity.ok(response);
    }
}
