package com.financeflow.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financeflow.account.model.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

    private UUID accountId;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private Instant lastUpdated;

    public static AccountBalanceResponse fromEntity(Account account) {
        return AccountBalanceResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .lastUpdated(account.getUpdatedAt())
                .build();
    }
}
