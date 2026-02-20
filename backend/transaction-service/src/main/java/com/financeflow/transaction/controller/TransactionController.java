package com.financeflow.transaction.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeflow.transaction.config.JwtAuthFilter.AuthenticatedUser;
import com.financeflow.transaction.dto.DepositRequest;
import com.financeflow.transaction.dto.PagedResponse;
import com.financeflow.transaction.dto.TransactionDto;
import com.financeflow.transaction.dto.TransferRequest;
import com.financeflow.transaction.dto.WithdrawRequest;
import com.financeflow.transaction.model.TransactionType;
import com.financeflow.transaction.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Deposits, withdrawals, transfers, and transaction history")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<TransactionDto>> listTransactions(
            @RequestParam UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, // FIXME: hardcoded page size should be configurable
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.debug("REST request to list transactions for account {} user {}", accountId, user.userId());
        PagedResponse<TransactionDto> transactions = transactionService.listTransactions(
                accountId, user.userId(), page, size, startDate, endDate, type, category
        );
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.debug("REST request to get transaction {} for user {}", id, user.userId());
        TransactionDto transaction = transactionService.getTransaction(id, user.userId());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionDto> deposit(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.debug("REST request to deposit to account {} for user {}", request.getAccountId(), user.userId());
        TransactionDto transaction = transactionService.deposit(request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDto> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.debug("REST request to withdraw from account {} for user {}", request.getAccountId(), user.userId());
        TransactionDto transaction = transactionService.withdraw(request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.debug("REST request to transfer from account {} to {} for user {}", 
                request.getFromAccountId(), request.getToAccountId(), user.userId());
        TransactionDto transaction = transactionService.transfer(request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
