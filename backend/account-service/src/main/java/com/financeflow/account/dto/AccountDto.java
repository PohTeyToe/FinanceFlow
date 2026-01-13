package com.financeflow.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financeflow.account.model.Account;
import com.financeflow.account.model.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private UUID id;
    private String accountNumber;
    private AccountType accountType;
    private String accountName;
    private BigDecimal balance;
    private String currency;
    private Boolean isActive;
    private Instant createdAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .accountName(account.getAccountName())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
