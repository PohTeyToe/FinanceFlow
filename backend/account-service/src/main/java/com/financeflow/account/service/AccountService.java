package com.financeflow.account.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeflow.account.dto.AccountBalanceResponse;
import com.financeflow.account.dto.AccountDto;
import com.financeflow.account.dto.AccountSummaryDto;
import com.financeflow.account.dto.CreateAccountRequest;
import com.financeflow.account.dto.UpdateAccountRequest;
import com.financeflow.account.exception.AccountNotFoundException;
import com.financeflow.account.model.Account;
import com.financeflow.account.model.AccountType;
import com.financeflow.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final Random random = new Random();

    /**
     * List all active accounts for a user
     */
    @Transactional(readOnly = true)
    public List<AccountSummaryDto> listAccounts(UUID userId) {
        log.debug("Listing accounts for user: {}", userId);
        return accountRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(AccountSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get account details by ID (with ownership check)
     */
    @Transactional(readOnly = true)
    public AccountDto getAccount(UUID accountId, UUID userId) {
        log.debug("Getting account {} for user {}", accountId, userId);
        Account account = findAccountByIdAndUser(accountId, userId);
        return AccountDto.fromEntity(account);
    }

    /**
     * Create a new account for a user
     */
    @Transactional
    public AccountDto createAccount(CreateAccountRequest request, UUID userId) {
        log.info("Creating {} account for user {}", request.getAccountType(), userId);

        String accountNumber = generateAccountNumber(userId, request.getAccountType());
        BigDecimal initialBalance = request.getInitialDeposit() != null 
                ? request.getInitialDeposit() 
                : BigDecimal.ZERO;

        String accountName = request.getAccountName();
        if (accountName == null || accountName.isBlank()) {
            accountName = generateDefaultAccountName(request.getAccountType());
        }

        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .accountName(accountName)
                .balance(initialBalance)
                .currency("USD")
                .isActive(true)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Created account {} with number {}", savedAccount.getId(), accountNumber);

        return AccountDto.fromEntity(savedAccount);
    }

    /**
     * Get account balance
     */
    @Transactional(readOnly = true)
    public AccountBalanceResponse getAccountBalance(UUID accountId, UUID userId) {
        log.debug("Getting balance for account {} user {}", accountId, userId);
        Account account = findAccountByIdAndUser(accountId, userId);
        return AccountBalanceResponse.fromEntity(account);
    }

    /**
     * Update account name
     */
    @Transactional
    public AccountDto updateAccount(UUID accountId, UpdateAccountRequest request, UUID userId) {
        log.info("Updating account {} for user {}", accountId, userId);
        Account account = findAccountByIdAndUser(accountId, userId);
        
        account.setAccountName(request.getAccountName());
        Account savedAccount = accountRepository.save(account);
        
        log.info("Updated account {} name to '{}'", accountId, request.getAccountName());
        return AccountDto.fromEntity(savedAccount);
    }

    /**
     * Deactivate an account (soft delete)
     */
    @Transactional
    public void deactivateAccount(UUID accountId, UUID userId) {
        log.info("Deactivating account {} for user {}", accountId, userId);
        Account account = findAccountByIdAndUser(accountId, userId);
        
        if (!account.getBalance().equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Cannot deactivate account with non-zero balance");
        }
        
        account.setIsActive(false);
        accountRepository.save(account);
        log.info("Deactivated account {}", accountId);
    }

    /**
     * Find account by ID ensuring it belongs to the user
     */
    private Account findAccountByIdAndUser(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .filter(Account::getIsActive)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * Generate unique account number in format: {TYPE}-{USER_SEQ}-{RANDOM}
     * Example: CHK-001-5839
     */
    private String generateAccountNumber(UUID userId, AccountType accountType) {
        String prefix = accountType.getPrefix();
        long userAccountCount = accountRepository.countByUserIdAndAccountType(userId, accountType);
        String sequence = String.format("%03d", userAccountCount + 1);
        
        String accountNumber;
        int attempts = 0;
        do {
            String randomPart = String.format("%04d", random.nextInt(10000));
            accountNumber = String.format("%s-%s-%s", prefix, sequence, randomPart);
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Failed to generate unique account number after 100 attempts");
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }

    /**
     * Generate default account name based on type
     */
    private String generateDefaultAccountName(AccountType accountType) {
        return switch (accountType) {
            case CHECKING -> "Checking Account";
            case SAVINGS -> "Savings Account";
            case CREDIT -> "Credit Account";
        };
    }
}
