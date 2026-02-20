package com.financeflow.account.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeflow.account.config.JwtAuthFilter.AuthenticatedUser;
import com.financeflow.account.dto.AccountBalanceResponse;
import com.financeflow.account.dto.AccountDto;
import com.financeflow.account.dto.AccountSummaryDto;
import com.financeflow.account.dto.CreateAccountRequest;
import com.financeflow.account.dto.UpdateAccountRequest;
import com.financeflow.account.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "Account creation, retrieval, and management")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountSummaryDto>> listAccounts(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.debug("REST request to list accounts for user: {}", user.userId());
        List<AccountSummaryDto> accounts = accountService.listAccounts(user.userId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.debug("REST request to get account: {} for user: {}", id, user.userId());
        AccountDto account = accountService.getAccount(id, user.userId());
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.debug("REST request to create account for user: {}", user.userId());
        AccountDto account = accountService.createAccount(request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.debug("REST request to get balance for account: {} user: {}", id, user.userId());
        AccountBalanceResponse balance = accountService.getAccountBalance(id, user.userId());
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.debug("REST request to update account: {} for user: {}", id, user.userId());
        AccountDto account = accountService.updateAccount(id, request, user.userId());
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.debug("REST request to deactivate account: {} for user: {}", id, user.userId());
        accountService.deactivateAccount(id, user.userId());
        return ResponseEntity.noContent().build();
    }
}
