package com.financeflow.account.dto;

import java.math.BigDecimal;

import com.financeflow.account.model.AccountType;

import jakarta.validation.constraints.DecimalMin;
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
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    @DecimalMin(value = "0.00", message = "Initial deposit must be zero or positive")
    @Builder.Default
    private BigDecimal initialDeposit = BigDecimal.ZERO;
}
