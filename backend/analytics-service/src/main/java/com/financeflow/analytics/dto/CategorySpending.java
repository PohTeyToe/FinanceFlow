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
public class CategorySpending {
    private String category;
    private BigDecimal amount;
    private BigDecimal percentage;
    private Long transactionCount;
}
