package com.financeflow.account.dto;

import java.math.BigDecimal;
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
public class AccountSummaryDto {

    private UUID id;
    private String accountNumber;
    private AccountType accountType;
    private String accountName;
    private BigDecimal balance;

    public static AccountSummaryDto fromEntity(Account account) {
        return AccountSummaryDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .accountName(account.getAccountName())
                .balance(account.getBalance())
                .build();
    }
}
