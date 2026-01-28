package com.financeflow.analytics.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyData {
    private String month;  // Format: "YYYY-MM"
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal netSavings;
}
