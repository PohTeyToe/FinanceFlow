package com.financeflow.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {
    private BigDecimal totalBalance;
    private List<AccountInfo> accounts;
    private BigDecimal thisMonthSpending;
    private BigDecimal thisMonthIncome;
    private String topSpendingCategory;
    private BigDecimal comparedToLastMonth;  // percentage change in spending
}
