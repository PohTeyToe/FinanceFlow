package com.financeflow.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Amount must not exceed 1,000,000")
    @Digits(integer = 7, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Size(max = 50, message = "Category must be at most 50 characters")
    private String category;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}
