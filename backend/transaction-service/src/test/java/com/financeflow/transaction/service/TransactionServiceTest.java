package com.financeflow.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.financeflow.transaction.dto.DepositRequest;
import com.financeflow.transaction.dto.PagedResponse;
import com.financeflow.transaction.dto.TransactionDto;
import com.financeflow.transaction.dto.TransferRequest;
import com.financeflow.transaction.dto.WithdrawRequest;
import com.financeflow.transaction.exception.AccountNotFoundException;
import com.financeflow.transaction.exception.InsufficientFundsException;
import com.financeflow.transaction.exception.TransactionNotFoundException;
import com.financeflow.transaction.model.Account;
import com.financeflow.transaction.model.AccountType;
import com.financeflow.transaction.model.Transaction;
import com.financeflow.transaction.model.TransactionStatus;
import com.financeflow.transaction.model.TransactionType;
import com.financeflow.transaction.repository.AccountRepository;
import com.financeflow.transaction.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID userId;
    private UUID accountId;
    private UUID destAccountId;
    private Account testAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        destAccountId = UUID.randomUUID();

        testAccount = Account.builder()
                .id(accountId)
                .userId(userId)
                .accountNumber("CHK-001-1234")
                .accountType(AccountType.CHECKING)
                .accountName("Primary Checking")
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        destAccount = Account.builder()
                .id(destAccountId)
                .userId(userId)
                .accountNumber("SAV-001-5678")
                .accountType(AccountType.SAVINGS)
                .accountName("Savings")
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Should successfully deposit funds")
        void shouldDepositFunds() {
            DepositRequest request = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("500.00"))
                    .category("Income")
                    .description("Salary")
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> {
                        Transaction t = inv.getArgument(0);
                        t.setId(UUID.randomUUID());
                        t.setCreatedAt(Instant.now());
                        return t;
                    });

            TransactionDto result = transactionService.deposit(request, userId);

            assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(result.getAmount()).isEqualByComparingTo("500.00");
            assertThat(result.getBalanceAfter()).isEqualByComparingTo("1500.00");
            assertThat(result.getCategory()).isEqualTo("Income");
            assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getBalance()).isEqualByComparingTo("1500.00");
        }

        @Test
        @DisplayName("Should throw exception for non-existent account")
        void shouldThrowForNonExistentAccount() {
            DepositRequest request = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("500.00"))
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.deposit(request, userId))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should use default category when not provided")
        void shouldUseDefaultCategory() {
            DepositRequest request = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("100.00"))
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> {
                        Transaction t = inv.getArgument(0);
                        t.setId(UUID.randomUUID());
                        t.setCreatedAt(Instant.now());
                        return t;
                    });

            TransactionDto result = transactionService.deposit(request, userId);

            assertThat(result.getCategory()).isEqualTo("Income");
        }
    }

    @Nested
    @DisplayName("Withdrawal Tests")
    class WithdrawalTests {

        @Test
        @DisplayName("Should successfully withdraw funds")
        void shouldWithdrawFunds() {
            WithdrawRequest request = WithdrawRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("200.00"))
                    .category("Food & Dining")
                    .description("Groceries")
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> {
                        Transaction t = inv.getArgument(0);
                        t.setId(UUID.randomUUID());
                        t.setCreatedAt(Instant.now());
                        return t;
                    });

            TransactionDto result = transactionService.withdraw(request, userId);

            assertThat(result.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(result.getAmount()).isEqualByComparingTo("200.00");
            assertThat(result.getBalanceAfter()).isEqualByComparingTo("800.00");
            assertThat(result.getCategory()).isEqualTo("Food & Dining");
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when balance too low")
        void shouldThrowInsufficientFunds() {
            WithdrawRequest request = WithdrawRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("1500.00"))
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> transactionService.withdraw(request, userId))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds")
                    .hasMessageContaining("1500.00")
                    .hasMessageContaining("1000.00");

            verify(accountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow withdrawal of exact balance")
        void shouldAllowExactBalanceWithdrawal() {
            WithdrawRequest request = WithdrawRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("1000.00"))
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> {
                        Transaction t = inv.getArgument(0);
                        t.setId(UUID.randomUUID());
                        t.setCreatedAt(Instant.now());
                        return t;
                    });

            TransactionDto result = transactionService.withdraw(request, userId);

            assertThat(result.getBalanceAfter()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {

        @Test
        @DisplayName("Should successfully transfer between own accounts")
        void shouldTransferBetweenOwnAccounts() {
            TransferRequest request = TransferRequest.builder()
                    .fromAccountId(accountId)
                    .toAccountId(destAccountId)
                    .amount(new BigDecimal("300.00"))
                    .description("Transfer to savings")
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.findByIdWithLock(destAccountId))
                    .thenReturn(Optional.of(destAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> {
                        Transaction t = inv.getArgument(0);
                        t.setId(UUID.randomUUID());
                        t.setCreatedAt(Instant.now());
                        return t;
                    });

            TransactionDto result = transactionService.transfer(request, userId);

            assertThat(result.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
            assertThat(result.getAmount()).isEqualByComparingTo("300.00");
            assertThat(result.getBalanceAfter()).isEqualByComparingTo("700.00");
            assertThat(result.getRecipientAccountId()).isEqualTo(destAccountId);

            // Verify both accounts were saved
            verify(accountRepository, times(2)).save(any(Account.class));
            // Verify both transactions were created
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception for same account transfer")
        void shouldThrowForSameAccountTransfer() {
            TransferRequest request = TransferRequest.builder()
                    .fromAccountId(accountId)
                    .toAccountId(accountId)
                    .amount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(() -> transactionService.transfer(request, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("same account");
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException on transfer")
        void shouldThrowInsufficientFundsOnTransfer() {
            TransferRequest request = TransferRequest.builder()
                    .fromAccountId(accountId)
                    .toAccountId(destAccountId)
                    .amount(new BigDecimal("2000.00"))
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.findByIdWithLock(destAccountId))
                    .thenReturn(Optional.of(destAccount));

            assertThatThrownBy(() -> transactionService.transfer(request, userId))
                    .isInstanceOf(InsufficientFundsException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for non-existent destination account")
        void shouldThrowForNonExistentDestAccount() {
            TransferRequest request = TransferRequest.builder()
                    .fromAccountId(accountId)
                    .toAccountId(destAccountId)
                    .amount(new BigDecimal("100.00"))
                    .build();

            when(accountRepository.findByIdAndUserIdWithLock(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.findByIdWithLock(destAccountId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(request, userId))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("List Transactions Tests")
    class ListTransactionsTests {

        @Test
        @DisplayName("Should list transactions with pagination")
        void shouldListTransactionsWithPagination() {
            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .accountId(accountId)
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("100.00"))
                    .balanceAfter(new BigDecimal("1100.00"))
                    .currency("USD")
                    .category("Income")
                    .referenceNumber("TXN-2024-001")
                    .status(TransactionStatus.COMPLETED)
                    .createdAt(Instant.now())
                    .build();

            Page<Transaction> page = new PageImpl<>(
                    List.of(transaction),
                    PageRequest.of(0, 20),
                    1
            );

            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(transactionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            PagedResponse<TransactionDto> result = transactionService.listTransactions(
                    accountId, userId, 0, 20, null, null, null, null
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw exception when account not owned by user")
        void shouldThrowWhenAccountNotOwned() {
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.listTransactions(
                    accountId, userId, 0, 20, null, null, null, null
            ))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Transaction Tests")
    class GetTransactionTests {

        @Test
        @DisplayName("Should get transaction by ID")
        void shouldGetTransactionById() {
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .id(transactionId)
                    .accountId(accountId)
                    .transactionType(TransactionType.WITHDRAWAL)
                    .amount(new BigDecimal("50.00"))
                    .balanceAfter(new BigDecimal("950.00"))
                    .currency("USD")
                    .category("Shopping")
                    .referenceNumber("TXN-2024-002")
                    .status(TransactionStatus.COMPLETED)
                    .createdAt(Instant.now())
                    .build();

            when(transactionRepository.findById(transactionId))
                    .thenReturn(Optional.of(transaction));
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));

            TransactionDto result = transactionService.getTransaction(transactionId, userId);

            assertThat(result.getId()).isEqualTo(transactionId);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(result.getAmount()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Should throw exception for non-existent transaction")
        void shouldThrowForNonExistentTransaction() {
            UUID transactionId = UUID.randomUUID();

            when(transactionRepository.findById(transactionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.getTransaction(transactionId, userId))
                    .isInstanceOf(TransactionNotFoundException.class);
        }
    }
}
