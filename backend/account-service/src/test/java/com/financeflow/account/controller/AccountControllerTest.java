package com.financeflow.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeflow.account.config.JwtAuthFilter.AuthenticatedUser;
import com.financeflow.account.config.SecurityConfig;
import com.financeflow.account.dto.AccountBalanceResponse;
import com.financeflow.account.dto.AccountDto;
import com.financeflow.account.dto.AccountSummaryDto;
import com.financeflow.account.dto.CreateAccountRequest;
import com.financeflow.account.dto.UpdateAccountRequest;
import com.financeflow.account.exception.AccountNotFoundException;
import com.financeflow.account.model.AccountType;
import com.financeflow.account.service.AccountService;
import com.financeflow.account.service.JwtService;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtService jwtService;

    private UUID userId;
    private UUID accountId;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        accountId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        authenticatedUser = new AuthenticatedUser(userId, "test@example.com");
        
        // Set up authentication for all tests
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("GET /api/accounts")
    class ListAccountsTests {

        @Test
        @DisplayName("Should return list of accounts")
        void shouldReturnAccounts() throws Exception {
            AccountSummaryDto account1 = AccountSummaryDto.builder()
                    .id(accountId)
                    .accountNumber("CHK-001-1234")
                    .accountType(AccountType.CHECKING)
                    .accountName("Primary Checking")
                    .balance(new BigDecimal("5420.50"))
                    .build();

            AccountSummaryDto account2 = AccountSummaryDto.builder()
                    .id(UUID.randomUUID())
                    .accountNumber("SAV-001-5678")
                    .accountType(AccountType.SAVINGS)
                    .accountName("Emergency Fund")
                    .balance(new BigDecimal("15000.00"))
                    .build();

            when(accountService.listAccounts(userId)).thenReturn(List.of(account1, account2));

            mockMvc.perform(get("/api/accounts")
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].accountNumber").value("CHK-001-1234"))
                    .andExpect(jsonPath("$[1].accountNumber").value("SAV-001-5678"));
        }

        @Test
        @DisplayName("Should return empty list when user has no accounts")
        void shouldReturnEmptyList() throws Exception {
            when(accountService.listAccounts(userId)).thenReturn(List.of());

            mockMvc.perform(get("/api/accounts")
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/accounts/{id}")
    class GetAccountTests {

        @Test
        @DisplayName("Should return account details")
        void shouldReturnAccountDetails() throws Exception {
            AccountDto account = AccountDto.builder()
                    .id(accountId)
                    .accountNumber("CHK-001-1234")
                    .accountType(AccountType.CHECKING)
                    .accountName("Primary Checking")
                    .balance(new BigDecimal("5420.50"))
                    .currency("USD")
                    .isActive(true)
                    .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                    .build();

            when(accountService.getAccount(accountId, userId)).thenReturn(account);

            mockMvc.perform(get("/api/accounts/{id}", accountId)
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(accountId.toString()))
                    .andExpect(jsonPath("$.accountNumber").value("CHK-001-1234"))
                    .andExpect(jsonPath("$.balance").value(5420.50))
                    .andExpect(jsonPath("$.currency").value("USD"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent account")
        void shouldReturn404ForNonExistent() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            when(accountService.getAccount(nonExistentId, userId))
                    .thenThrow(new AccountNotFoundException(nonExistentId));

            mockMvc.perform(get("/api/accounts/{id}", nonExistentId)
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Account not found"));
        }
    }

    @Nested
    @DisplayName("POST /api/accounts")
    class CreateAccountTests {

        @Test
        @DisplayName("Should create new checking account")
        void shouldCreateCheckingAccount() throws Exception {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountType(AccountType.CHECKING)
                    .accountName("Primary Checking")
                    .initialDeposit(new BigDecimal("100.00"))
                    .build();

            AccountDto response = AccountDto.builder()
                    .id(UUID.randomUUID())
                    .accountNumber("CHK-001-5839")
                    .accountType(AccountType.CHECKING)
                    .accountName("Primary Checking")
                    .balance(new BigDecimal("100.00"))
                    .currency("USD")
                    .isActive(true)
                    .createdAt(Instant.now())
                    .build();

            when(accountService.createAccount(any(CreateAccountRequest.class), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountType").value("CHECKING"))
                    .andExpect(jsonPath("$.accountName").value("Primary Checking"))
                    .andExpect(jsonPath("$.balance").value(100.00));
        }

        @Test
        @DisplayName("Should create savings account without initial deposit")
        void shouldCreateSavingsAccountWithoutDeposit() throws Exception {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountType(AccountType.SAVINGS)
                    .accountName("Emergency Fund")
                    .build();

            AccountDto response = AccountDto.builder()
                    .id(UUID.randomUUID())
                    .accountNumber("SAV-001-1234")
                    .accountType(AccountType.SAVINGS)
                    .accountName("Emergency Fund")
                    .balance(BigDecimal.ZERO)
                    .currency("USD")
                    .isActive(true)
                    .createdAt(Instant.now())
                    .build();

            when(accountService.createAccount(any(CreateAccountRequest.class), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("Should return 400 for missing account type")
        void shouldReturn400ForMissingType() throws Exception {
            String invalidRequest = "{\"accountName\": \"Test\"}";

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest)
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/accounts/{id}/balance")
    class GetBalanceTests {

        @Test
        @DisplayName("Should return account balance")
        void shouldReturnBalance() throws Exception {
            AccountBalanceResponse balance = AccountBalanceResponse.builder()
                    .accountId(accountId)
                    .accountNumber("CHK-001-1234")
                    .balance(new BigDecimal("5420.50"))
                    .currency("USD")
                    .lastUpdated(Instant.parse("2024-01-15T10:30:00Z"))
                    .build();

            when(accountService.getAccountBalance(accountId, userId)).thenReturn(balance);

            mockMvc.perform(get("/api/accounts/{id}/balance", accountId)
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                    .andExpect(jsonPath("$.balance").value(5420.50))
                    .andExpect(jsonPath("$.currency").value("USD"));
        }
    }

    @Nested
    @DisplayName("PUT /api/accounts/{id}")
    class UpdateAccountTests {

        @Test
        @DisplayName("Should update account name")
        void shouldUpdateAccountName() throws Exception {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountName("Updated Account Name")
                    .build();

            AccountDto response = AccountDto.builder()
                    .id(accountId)
                    .accountNumber("CHK-001-1234")
                    .accountType(AccountType.CHECKING)
                    .accountName("Updated Account Name")
                    .balance(new BigDecimal("5420.50"))
                    .currency("USD")
                    .isActive(true)
                    .createdAt(Instant.now())
                    .build();

            when(accountService.updateAccount(eq(accountId), any(UpdateAccountRequest.class), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/accounts/{id}", accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountName").value("Updated Account Name"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/accounts/{id}")
    class DeactivateAccountTests {

        @Test
        @DisplayName("Should deactivate account")
        void shouldDeactivateAccount() throws Exception {
            mockMvc.perform(delete("/api/accounts/{id}", accountId)
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isNoContent());

            verify(accountService).deactivateAccount(accountId, userId);
        }

        @Test
        @DisplayName("Should return 400 for non-zero balance")
        void shouldReturn400ForNonZeroBalance() throws Exception {
            doThrow(new IllegalArgumentException("Cannot deactivate account with non-zero balance"))
                    .when(accountService).deactivateAccount(accountId, userId);

            mockMvc.perform(delete("/api/accounts/{id}", accountId)
                            .principal(new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cannot deactivate account with non-zero balance"));
        }
    }
}
