package com.financeflow.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
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
import com.financeflow.transaction.config.JwtAuthFilter.AuthenticatedUser;
import com.financeflow.transaction.config.SecurityConfig;
import com.financeflow.transaction.dto.DepositRequest;
import com.financeflow.transaction.dto.PagedResponse;
import com.financeflow.transaction.dto.TransactionDto;
import com.financeflow.transaction.dto.TransferRequest;
import com.financeflow.transaction.dto.WithdrawRequest;
import com.financeflow.transaction.exception.InsufficientFundsException;
import com.financeflow.transaction.model.TransactionStatus;
import com.financeflow.transaction.model.TransactionType;
import com.financeflow.transaction.service.JwtService;
import com.financeflow.transaction.service.TransactionService;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    private UUID userId;
    private UUID accountId;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        authenticatedUser = new AuthenticatedUser(userId, "test@example.com");

        // Set up security context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser, null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Deposit Endpoint")
    class DepositEndpointTests {

        @Test
        @DisplayName("Should deposit successfully")
        void shouldDepositSuccessfully() throws Exception {
            DepositRequest request = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("500.00"))
                    .category("Income")
                    .description("Salary")
                    .build();

            TransactionDto response = TransactionDto.builder()
                    .id(UUID.randomUUID())
                    .accountId(accountId)
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("500.00"))
                    .balanceAfter(new BigDecimal("1500.00"))
                    .currency("USD")
                    .category("Income")
                    .description("Salary")
                    .referenceNumber("TXN-2024-001")
                    .status(TransactionStatus.COMPLETED)
                    .createdAt(Instant.now())
                    .build();

            when(transactionService.deposit(any(DepositRequest.class), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/transactions/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                    .andExpect(jsonPath("$.amount").value(500.00))
                    .andExpect(jsonPath("$.balanceAfter").value(1500.00))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.category").value("Income"))
                    .andExpect(jsonPath("$.description").value("Salary"))
                    .andExpect(jsonPath("$.referenceNumber").value("TXN-2024-001"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 400 for invalid deposit request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            DepositRequest request = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("-100.00"))
                    .build();

            mockMvc.perform(post("/api/transactions/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for zero deposit amount")
        void shouldReturn400ForZeroAmount() throws Exception {
            DepositRequest request = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(BigDecimal.ZERO)
                    .build();

            mockMvc.perform(post("/api/transactions/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing account ID")
        void shouldReturn400ForMissingAccountId() throws Exception {
            DepositRequest request = DepositRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .build();

            mockMvc.perform(post("/api/transactions/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Withdraw Endpoint")
    class WithdrawEndpointTests {

        @Test
        @DisplayName("Should withdraw successfully")
        void shouldWithdrawSuccessfully() throws Exception {
            WithdrawRequest request = WithdrawRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("100.00"))
                    .category("Shopping")
                    .build();

            TransactionDto response = TransactionDto.builder()
                    .id(UUID.randomUUID())
                    .accountId(accountId)
                    .transactionType(TransactionType.WITHDRAWAL)
                    .amount(new BigDecimal("100.00"))
                    .balanceAfter(new BigDecimal("900.00"))
                    .currency("USD")
                    .category("Shopping")
                    .referenceNumber("TXN-2024-002")
                    .status(TransactionStatus.COMPLETED)
                    .createdAt(Instant.now())
                    .build();

            when(transactionService.withdraw(any(WithdrawRequest.class), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/transactions/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"))
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.balanceAfter").value(900.00))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.category").value("Shopping"))
                    .andExpect(jsonPath("$.referenceNumber").value("TXN-2024-002"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.accountId").value(accountId.toString()));
        }

        @Test
        @DisplayName("Should return 400 for insufficient funds")
        void shouldReturn400ForInsufficientFunds() throws Exception {
            WithdrawRequest request = WithdrawRequest.builder()
                    .accountId(accountId)
                    .amount(new BigDecimal("10000.00"))
                    .build();

            when(transactionService.withdraw(any(WithdrawRequest.class), eq(userId)))
                    .thenThrow(new InsufficientFundsException(accountId, 
                            new BigDecimal("10000.00"), new BigDecimal("1000.00")));

            mockMvc.perform(post("/api/transactions/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Insufficient funds")));
        }
    }

    @Nested
    @DisplayName("Transfer Endpoint")
    class TransferEndpointTests {

        @Test
        @DisplayName("Should transfer successfully")
        void shouldTransferSuccessfully() throws Exception {
            UUID destAccountId = UUID.randomUUID();
            TransferRequest request = TransferRequest.builder()
                    .fromAccountId(accountId)
                    .toAccountId(destAccountId)
                    .amount(new BigDecimal("200.00"))
                    .description("Transfer to savings")
                    .build();

            TransactionDto response = TransactionDto.builder()
                    .id(UUID.randomUUID())
                    .accountId(accountId)
                    .transactionType(TransactionType.TRANSFER_OUT)
                    .amount(new BigDecimal("200.00"))
                    .balanceAfter(new BigDecimal("800.00"))
                    .currency("USD")
                    .category("Transfer")
                    .description("Transfer to savings")
                    .recipientAccountId(destAccountId)
                    .referenceNumber("TXN-2024-003-OUT")
                    .status(TransactionStatus.COMPLETED)
                    .createdAt(Instant.now())
                    .build();

            when(transactionService.transfer(any(TransferRequest.class), eq(userId), isNull()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/transactions/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionType").value("TRANSFER_OUT"))
                    .andExpect(jsonPath("$.amount").value(200.00))
                    .andExpect(jsonPath("$.balanceAfter").value(800.00))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.description").value("Transfer to savings"))
                    .andExpect(jsonPath("$.recipientAccountId").value(destAccountId.toString()))
                    .andExpect(jsonPath("$.referenceNumber").value("TXN-2024-003-OUT"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.accountId").value(accountId.toString()));
        }
    }

    @Nested
    @DisplayName("List Transactions Endpoint")
    class ListTransactionsEndpointTests {

        @Test
        @DisplayName("Should list transactions with pagination")
        void shouldListTransactionsWithPagination() throws Exception {
            TransactionDto transaction = TransactionDto.builder()
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

            PagedResponse<TransactionDto> response = PagedResponse.<TransactionDto>builder()
                    .content(List.of(transaction))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .build();

            when(transactionService.listTransactions(
                    eq(accountId), eq(userId), eq(0), eq(20), any(), any(), any(), any()))
                    .thenReturn(response);

            mockMvc.perform(get("/api/transactions")
                            .param("accountId", accountId.toString())
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("Get Transaction Endpoint")
    class GetTransactionEndpointTests {

        @Test
        @DisplayName("Should get transaction by ID")
        void shouldGetTransactionById() throws Exception {
            UUID transactionId = UUID.randomUUID();
            TransactionDto response = TransactionDto.builder()
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

            when(transactionService.getTransaction(transactionId, userId))
                    .thenReturn(response);

            mockMvc.perform(get("/api/transactions/{id}", transactionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(transactionId.toString()))
                    .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));
        }
    }
}
