package com.financeflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingByCategoryResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalSpending;
    private List<CategorySpending> categories;
}
