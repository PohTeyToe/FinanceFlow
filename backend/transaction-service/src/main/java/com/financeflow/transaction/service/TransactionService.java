package com.financeflow.transaction.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeflow.transaction.dto.DepositRequest;
import com.financeflow.transaction.dto.PagedResponse;
import com.financeflow.transaction.dto.TransactionDto;
import com.financeflow.transaction.dto.TransferRequest;
import com.financeflow.transaction.dto.WithdrawRequest;
import com.financeflow.transaction.exception.AccountNotFoundException;
import com.financeflow.transaction.exception.InsufficientFundsException;
import com.financeflow.transaction.exception.TransactionNotFoundException;
import com.financeflow.transaction.exception.UnauthorizedAccessException;
import com.financeflow.transaction.model.Account;
import com.financeflow.transaction.model.Transaction;
import com.financeflow.transaction.model.TransactionStatus;
import com.financeflow.transaction.model.TransactionType;
import com.financeflow.transaction.repository.AccountRepository;
import com.financeflow.transaction.repository.TransactionRepository;
import com.financeflow.transaction.repository.TransactionSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // Counter for generating unique reference numbers
    private final AtomicLong referenceCounter = new AtomicLong(System.currentTimeMillis());

    @Transactional(readOnly = true)
    public PagedResponse<TransactionDto> listTransactions(
            UUID accountId,
            UUID userId,
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            String category
    ) {
        log.debug("Listing transactions for account {} user {} page {} size {}", accountId, userId, page, size);

        // Resolve account IDs: either verify specific account or get all user accounts
        List<UUID> accountIds = null;
        if (accountId != null) {
            verifyAccountOwnership(accountId, userId);
        } else {
            List<Account> userAccounts = accountRepository.findAllByUserIdAndIsActiveTrue(userId);
            accountIds = userAccounts.stream().map(Account::getId).toList();
        }

        Pageable pageable = PageRequest.of(page, size);

        // Convert dates to Instant
        Instant startInstant = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        // Convert enum to string for specification
        String typeStr = type != null ? type.name() : null;

        // Build specification and execute query
        Specification<Transaction> spec = TransactionSpecification.withFilters(
                accountId,
                accountIds,
                startInstant,
                endInstant,
                typeStr,
                category
        );

        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        Page<TransactionDto> dtoPage = transactionPage.map(TransactionDto::fromEntity);
        return PagedResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransaction(UUID transactionId, UUID userId) {
        log.debug("Getting transaction {} for user {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        // Verify user owns the account
        verifyAccountOwnership(transaction.getAccountId(), userId);

        return TransactionDto.fromEntity(transaction);
    }

    // TODO: add audit logging for balance changes
    @Transactional
    public TransactionDto deposit(DepositRequest request, UUID userId) {
        log.info("Processing deposit of {} to account {} for user {}", 
                request.getAmount(), request.getAccountId(), userId);

        // Get account with lock to ensure atomicity
        Account account = accountRepository.findByIdAndUserIdWithLock(request.getAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        // Update balance
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .currency(account.getCurrency())
                .category(request.getCategory() != null ? request.getCategory() : "Income")
                .description(request.getDescription())
                .referenceNumber(generateReferenceNumber())
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Deposit completed: {} balance now {}", savedTransaction.getReferenceNumber(), newBalance);

        return TransactionDto.fromEntity(savedTransaction);
    }

    @Transactional
    public TransactionDto withdraw(WithdrawRequest request, UUID userId) {
        log.info("Processing withdrawal of {} from account {} for user {}", 
                request.getAmount(), request.getAccountId(), userId);

        // Get account with lock
        Account account = accountRepository.findByIdAndUserIdWithLock(request.getAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        // Check sufficient funds
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    account.getId(),
                    request.getAmount(),
                    account.getBalance()
            );
        }

        // Update balance
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .currency(account.getCurrency())
                .category(request.getCategory() != null ? request.getCategory() : "Other")
                .description(request.getDescription())
                .referenceNumber(generateReferenceNumber())
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Withdrawal completed: {} balance now {}", savedTransaction.getReferenceNumber(), newBalance);

        return TransactionDto.fromEntity(savedTransaction);
    }

    @Transactional
    public TransactionDto transfer(TransferRequest request, UUID userId) {
        log.info("Processing transfer of {} from account {} to account {} for user {}", 
                request.getAmount(), request.getFromAccountId(), request.getToAccountId(), userId);

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Get source account with lock (must belong to user)
        Account sourceAccount = accountRepository.findByIdAndUserIdWithLock(request.getFromAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));

        // Get destination account with lock (can be any active account)
        Account destAccount = accountRepository.findByIdWithLock(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccountId()));

        // Verify destination is either owned by same user or is a valid internal account
        boolean isInternalTransfer = destAccount.getUserId().equals(userId);
        if (!isInternalTransfer && !destAccount.getIsActive()) {
            throw new UnauthorizedAccessException("Cannot transfer to this account");
        }

        // Check sufficient funds
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    sourceAccount.getId(),
                    request.getAmount(),
                    sourceAccount.getBalance()
            );
        }

        // Generate a shared reference number for the transfer pair
        String referenceNumber = generateReferenceNumber();

        // Update source account
        BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(request.getAmount());
        sourceAccount.setBalance(sourceNewBalance);
        accountRepository.save(sourceAccount);

        // Update destination account
        BigDecimal destNewBalance = destAccount.getBalance().add(request.getAmount());
        destAccount.setBalance(destNewBalance);
        accountRepository.save(destAccount);

        // Create TRANSFER_OUT transaction for source account
        Transaction outTransaction = Transaction.builder()
                .accountId(sourceAccount.getId())
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(request.getAmount())
                .balanceAfter(sourceNewBalance)
                .currency(sourceAccount.getCurrency())
                .category("Transfer")
                .description(request.getDescription())
                .recipientAccountId(destAccount.getId())
                .referenceNumber(referenceNumber + "-OUT")
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction savedOutTransaction = transactionRepository.save(outTransaction);

        // Create TRANSFER_IN transaction for destination account
        Transaction inTransaction = Transaction.builder()
                .accountId(destAccount.getId())
                .transactionType(TransactionType.TRANSFER_IN)
                .amount(request.getAmount())
                .balanceAfter(destNewBalance)
                .currency(destAccount.getCurrency())
                .category("Transfer")
                .description(request.getDescription())
                .recipientAccountId(sourceAccount.getId())
                .referenceNumber(referenceNumber + "-IN")
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(inTransaction);

        log.info("Transfer completed: {} from {} to {}", referenceNumber, 
                sourceAccount.getId(), destAccount.getId());

        // Return the TRANSFER_OUT transaction
        return TransactionDto.fromEntity(savedOutTransaction);
    }

    private void verifyAccountOwnership(UUID accountId, UUID userId) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .filter(Account::getIsActive)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private String generateReferenceNumber() {
        int year = LocalDate.now().getYear();
        long counter = referenceCounter.incrementAndGet();
        return String.format("TXN-%d-%06d", year, counter % 1000000);
    }
}
