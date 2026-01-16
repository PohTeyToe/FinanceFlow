package com.financeflow.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeflow.account.dto.AccountBalanceResponse;
import com.financeflow.account.dto.AccountDto;
import com.financeflow.account.dto.AccountSummaryDto;
import com.financeflow.account.dto.CreateAccountRequest;
import com.financeflow.account.dto.UpdateAccountRequest;
import com.financeflow.account.exception.AccountNotFoundException;
import com.financeflow.account.model.Account;
import com.financeflow.account.model.AccountType;
import com.financeflow.account.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;
    private UUID accountId;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        
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
    }

    @Nested
    @DisplayName("List Accounts")
    class ListAccountsTests {

        @Test
        @DisplayName("Should return list of account summaries for user")
        void shouldReturnAccountSummaries() {
            Account savingsAccount = Account.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .accountNumber("SAV-001-5678")
                    .accountType(AccountType.SAVINGS)
                    .accountName("Emergency Fund")
                    .balance(new BigDecimal("5000.00"))
                    .currency("USD")
                    .isActive(true)
                    .build();

            when(accountRepository.findByUserIdAndIsActiveTrue(userId))
                    .thenReturn(List.of(testAccount, savingsAccount));

            List<AccountSummaryDto> result = accountService.listAccounts(userId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAccountType()).isEqualTo(AccountType.CHECKING);
            assertThat(result.get(1).getAccountType()).isEqualTo(AccountType.SAVINGS);
        }

        @Test
        @DisplayName("Should return empty list when user has no accounts")
        void shouldReturnEmptyList() {
            when(accountRepository.findByUserIdAndIsActiveTrue(userId))
                    .thenReturn(List.of());

            List<AccountSummaryDto> result = accountService.listAccounts(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Account")
    class GetAccountTests {

        @Test
        @DisplayName("Should return account details for own account")
        void shouldReturnOwnAccount() {
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));

            AccountDto result = accountService.getAccount(accountId, userId);

            assertThat(result.getId()).isEqualTo(accountId);
            assertThat(result.getAccountNumber()).isEqualTo("CHK-001-1234");
            assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
            assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException for non-existent account")
        void shouldThrowNotFoundForNonExistent() {
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccount(accountId, userId))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException for inactive account")
        void shouldThrowNotFoundForInactiveAccount() {
            testAccount.setIsActive(false);
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> accountService.getAccount(accountId, userId))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create Account")
    class CreateAccountTests {

        @Test
        @DisplayName("Should create checking account")
        void shouldCreateCheckingAccount() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountType(AccountType.CHECKING)
                    .accountName("My Checking")
                    .initialDeposit(new BigDecimal("100.00"))
                    .build();

            when(accountRepository.countByUserIdAndAccountType(userId, AccountType.CHECKING))
                    .thenReturn(0L);
            when(accountRepository.existsByAccountNumber(any()))
                    .thenReturn(false);
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> {
                        Account a = inv.getArgument(0);
                        a.setId(UUID.randomUUID());
                        a.setCreatedAt(Instant.now());
                        a.setUpdatedAt(Instant.now());
                        return a;
                    });

            AccountDto result = accountService.createAccount(request, userId);

            assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
            assertThat(result.getAccountName()).isEqualTo("My Checking");
            assertThat(result.getBalance()).isEqualByComparingTo("100.00");
            assertThat(result.getAccountNumber()).startsWith("CHK-001-");
        }

        @Test
        @DisplayName("Should create savings account")
        void shouldCreateSavingsAccount() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountType(AccountType.SAVINGS)
                    .build();

            when(accountRepository.countByUserIdAndAccountType(userId, AccountType.SAVINGS))
                    .thenReturn(0L);
            when(accountRepository.existsByAccountNumber(any()))
                    .thenReturn(false);
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> {
                        Account a = inv.getArgument(0);
                        a.setId(UUID.randomUUID());
                        a.setCreatedAt(Instant.now());
                        a.setUpdatedAt(Instant.now());
                        return a;
                    });

            AccountDto result = accountService.createAccount(request, userId);

            assertThat(result.getAccountType()).isEqualTo(AccountType.SAVINGS);
            assertThat(result.getAccountName()).isEqualTo("Savings Account");
            assertThat(result.getBalance()).isEqualByComparingTo("0.00");
            assertThat(result.getAccountNumber()).startsWith("SAV-001-");
        }

        @Test
        @DisplayName("Should create credit account")
        void shouldCreateCreditAccount() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountType(AccountType.CREDIT)
                    .accountName("Rewards Card")
                    .build();

            when(accountRepository.countByUserIdAndAccountType(userId, AccountType.CREDIT))
                    .thenReturn(0L);
            when(accountRepository.existsByAccountNumber(any()))
                    .thenReturn(false);
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> {
                        Account a = inv.getArgument(0);
                        a.setId(UUID.randomUUID());
                        a.setCreatedAt(Instant.now());
                        a.setUpdatedAt(Instant.now());
                        return a;
                    });

            AccountDto result = accountService.createAccount(request, userId);

            assertThat(result.getAccountType()).isEqualTo(AccountType.CREDIT);
            assertThat(result.getAccountName()).isEqualTo("Rewards Card");
            assertThat(result.getAccountNumber()).startsWith("CRD-001-");
        }
    }

    @Nested
    @DisplayName("Get Account Balance")
    class GetBalanceTests {

        @Test
        @DisplayName("Should return balance for own account")
        void shouldReturnBalance() {
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));

            AccountBalanceResponse result = accountService.getAccountBalance(accountId, userId);

            assertThat(result.getAccountId()).isEqualTo(accountId);
            assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
            assertThat(result.getCurrency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("Update Account")
    class UpdateAccountTests {

        @Test
        @DisplayName("Should update account name")
        void shouldUpdateAccountName() {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountName("Updated Account Name")
                    .build();

            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AccountDto result = accountService.updateAccount(accountId, request, userId);

            assertThat(result.getAccountName()).isEqualTo("Updated Account Name");
            verify(accountRepository).save(testAccount);
        }
    }

    @Nested
    @DisplayName("Deactivate Account")
    class DeactivateAccountTests {

        @Test
        @DisplayName("Should deactivate account with zero balance")
        void shouldDeactivateZeroBalanceAccount() {
            testAccount.setBalance(BigDecimal.ZERO);
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            accountService.deactivateAccount(accountId, userId);

            assertThat(testAccount.getIsActive()).isFalse();
            verify(accountRepository).save(testAccount);
        }

        @Test
        @DisplayName("Should throw exception for non-zero balance account")
        void shouldThrowForNonZeroBalance() {
            when(accountRepository.findByIdAndUserId(accountId, userId))
                    .thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> accountService.deactivateAccount(accountId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-zero balance");
            
            verify(accountRepository, never()).save(any());
        }
    }
}
