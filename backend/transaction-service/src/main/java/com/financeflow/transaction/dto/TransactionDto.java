package com.financeflow.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financeflow.transaction.model.Transaction;
import com.financeflow.transaction.model.TransactionStatus;
import com.financeflow.transaction.model.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private UUID id;
    private UUID accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String currency;
    private String category;
    private String description;
    private UUID recipientAccountId;
    private String referenceNumber;
    private TransactionStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .currency(transaction.getCurrency())
                .category(transaction.getCategory())
                .description(transaction.getDescription())
                .recipientAccountId(transaction.getRecipientAccountId())
                .referenceNumber(transaction.getReferenceNumber())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
