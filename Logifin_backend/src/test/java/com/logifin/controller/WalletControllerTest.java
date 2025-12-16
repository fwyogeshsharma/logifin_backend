package com.logifin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logifin.dto.*;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.*;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.security.JwtTokenProvider;
import com.logifin.security.UserPrincipal;
import com.logifin.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("WalletController Tests")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockBean
    private WalletService walletService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    private String adminToken;
    private String superAdminToken;
    private String userToken;
    private WalletDTO testWalletDTO;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        Role adminRole = Role.builder()
                .roleName("ROLE_ADMIN")
                .description("Admin Role")
                .build();
        adminRole.setId(1L);

        Role superAdminRole = Role.builder()
                .roleName("ROLE_SUPER_ADMIN")
                .description("Super Admin Role")
                .build();
        superAdminRole.setId(2L);

        Role userRole = Role.builder()
                .roleName("ROLE_USER")
                .description("User Role")
                .build();
        userRole.setId(3L);

        adminUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@test.com")
                .password("password")
                .active(true)
                .role(adminRole)
                .build();
        adminUser.setId(1L);

        User superAdminUser = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("superadmin@test.com")
                .password("password")
                .active(true)
                .role(superAdminRole)
                .build();
        superAdminUser.setId(2L);

        regularUser = User.builder()
                .firstName("Regular")
                .lastName("User")
                .email("user@test.com")
                .password("password")
                .active(true)
                .role(userRole)
                .build();
        regularUser.setId(3L);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(superAdminRole));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(userRole));

        adminToken = generateToken(adminUser, "ROLE_ADMIN");
        superAdminToken = generateToken(superAdminUser, "ROLE_SUPER_ADMIN");
        userToken = generateToken(regularUser, "ROLE_USER");

        testWalletDTO = WalletDTO.builder()
                .walletId(1L)
                .userId(3L)
                .currencyCode("INR")
                .status("ACTIVE")
                .currentBalance(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private String generateToken(User user, String role) {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        return tokenProvider.generateToken(authentication);
    }

    @Nested
    @DisplayName("Create Wallet Tests")
    class CreateWalletTests {

        @Test
        @DisplayName("Should create wallet successfully with admin token")
        void shouldCreateWalletSuccessfullyWithAdminToken() throws Exception {
            CreateWalletRequest request = CreateWalletRequest.builder()
                    .userId(3L)
                    .currencyCode("INR")
                    .build();

            when(walletService.createWallet(any(CreateWalletRequest.class), anyLong()))
                    .thenReturn(testWalletDTO);

            mockMvc.perform(post("/api/v1/wallets")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Wallet created successfully"))
                    .andExpect(jsonPath("$.data.walletId").value(1))
                    .andExpect(jsonPath("$.data.userId").value(3))
                    .andExpect(jsonPath("$.data.currencyCode").value("INR"));

            verify(walletService).createWallet(any(CreateWalletRequest.class), anyLong());
        }

        @Test
        @DisplayName("Should fail to create wallet without admin role")
        void shouldFailToCreateWalletWithoutAdminRole() throws Exception {
            CreateWalletRequest request = CreateWalletRequest.builder()
                    .userId(3L)
                    .currencyCode("INR")
                    .build();

            mockMvc.perform(post("/api/v1/wallets")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(walletService, never()).createWallet(any(), anyLong());
        }

        @Test
        @DisplayName("Should return 409 when wallet already exists")
        void shouldReturn409WhenWalletAlreadyExists() throws Exception {
            CreateWalletRequest request = CreateWalletRequest.builder()
                    .userId(3L)
                    .currencyCode("INR")
                    .build();

            when(walletService.createWallet(any(CreateWalletRequest.class), anyLong()))
                    .thenThrow(new DuplicateResourceException("Wallet already exists for user: 3"));

            mockMvc.perform(post("/api/v1/wallets")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Wallet already exists")));
        }
    }

    @Nested
    @DisplayName("Get Wallet Tests")
    class GetWalletTests {

        @Test
        @DisplayName("Should get wallet by user ID successfully")
        void shouldGetWalletByUserIdSuccessfully() throws Exception {
            when(walletService.getWalletByUserId(3L)).thenReturn(testWalletDTO);

            mockMvc.perform(get("/api/v1/wallets/user/3")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.walletId").value(1))
                    .andExpect(jsonPath("$.data.userId").value(3));

            verify(walletService).getWalletByUserId(3L);
        }

        @Test
        @DisplayName("Should return 404 when wallet not found")
        void shouldReturn404WhenWalletNotFound() throws Exception {
            when(walletService.getWalletByUserId(999L))
                    .thenThrow(new WalletNotFoundException("Wallet not found for user: 999"));

            mockMvc.perform(get("/api/v1/wallets/user/999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Wallet not found")));
        }
    }

    @Nested
    @DisplayName("Get Balance Tests")
    class GetBalanceTests {

        @Test
        @DisplayName("Should get wallet balance successfully")
        void shouldGetWalletBalanceSuccessfully() throws Exception {
            WalletBalanceDTO balanceDTO = WalletBalanceDTO.builder()
                    .walletId(1L)
                    .userId(3L)
                    .currencyCode("INR")
                    .availableBalance(new BigDecimal("5000.00"))
                    .status("ACTIVE")
                    .asOfTime(LocalDateTime.now())
                    .build();

            when(walletService.getWalletBalance(3L)).thenReturn(balanceDTO);

            mockMvc.perform(get("/api/v1/wallets/balance/3")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.availableBalance").value(5000.00));

            verify(walletService).getWalletBalance(3L);
        }
    }

    @Nested
    @DisplayName("Manual Credit Tests")
    class ManualCreditTests {

        @Test
        @DisplayName("Should process manual credit successfully")
        void shouldProcessManualCreditSuccessfully() throws Exception {
            ManualCreditRequest request = ManualCreditRequest.builder()
                    .userId(3L)
                    .amount(new BigDecimal("1000.00"))
                    .paymentMethod("UPI")
                    .referenceNumber("UPI123456")
                    .remarks("Test deposit")
                    .build();

            TransactionResponseDTO response = TransactionResponseDTO.builder()
                    .transactionId(UUID.randomUUID())
                    .transactionType("MANUAL_CREDIT")
                    .status("COMPLETED")
                    .amount(new BigDecimal("1000.00"))
                    .createdByUserId(1L)
                    .createdAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();

            when(walletService.processManualCredit(any(ManualCreditRequest.class), anyLong()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/wallets/credit")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Credit processed successfully"))
                    .andExpect(jsonPath("$.data.transactionType").value("MANUAL_CREDIT"));

            verify(walletService).processManualCredit(any(ManualCreditRequest.class), anyLong());
        }

        @Test
        @DisplayName("Should fail credit with user role")
        void shouldFailCreditWithUserRole() throws Exception {
            ManualCreditRequest request = ManualCreditRequest.builder()
                    .userId(3L)
                    .amount(new BigDecimal("1000.00"))
                    .build();

            mockMvc.perform(post("/api/v1/wallets/credit")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(walletService, never()).processManualCredit(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("Manual Debit Tests")
    class ManualDebitTests {

        @Test
        @DisplayName("Should process manual debit successfully")
        void shouldProcessManualDebitSuccessfully() throws Exception {
            ManualDebitRequest request = ManualDebitRequest.builder()
                    .userId(3L)
                    .amount(new BigDecimal("500.00"))
                    .paymentMethod("BANK_TRANSFER")
                    .referenceNumber("TXN123456")
                    .remarks("Test withdrawal")
                    .build();

            TransactionResponseDTO response = TransactionResponseDTO.builder()
                    .transactionId(UUID.randomUUID())
                    .transactionType("MANUAL_DEBIT")
                    .status("COMPLETED")
                    .amount(new BigDecimal("500.00"))
                    .createdByUserId(1L)
                    .createdAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();

            when(walletService.processManualDebit(any(ManualDebitRequest.class), anyLong()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/wallets/debit")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.transactionType").value("MANUAL_DEBIT"));

            verify(walletService).processManualDebit(any(ManualDebitRequest.class), anyLong());
        }

        @Test
        @DisplayName("Should allow debit with negative balance (borrowing)")
        void shouldAllowDebitWithNegativeBalance() throws Exception {
            ManualDebitRequest request = ManualDebitRequest.builder()
                    .userId(3L)
                    .amount(new BigDecimal("10000.00"))
                    .paymentMethod("BANK_TRANSFER")
                    .referenceNumber("TXN999")
                    .remarks("Test borrowing")
                    .build();

            TransactionResponseDTO response = TransactionResponseDTO.builder()
                    .transactionId(UUID.randomUUID())
                    .transactionType("MANUAL_DEBIT")
                    .status("COMPLETED")
                    .amount(new BigDecimal("10000.00"))
                    .description("Manual debit of 10000.00 INR (Borrowing: 9900.00 INR)")
                    .createdByUserId(1L)
                    .createdAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();

            when(walletService.processManualDebit(any(ManualDebitRequest.class), anyLong()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/wallets/debit")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.transactionType").value("MANUAL_DEBIT"))
                    .andExpect(jsonPath("$.data.description").value(containsString("Borrowing")));

            verify(walletService).processManualDebit(any(ManualDebitRequest.class), anyLong());
        }
    }

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {

        @Test
        @DisplayName("Should process transfer successfully")
        void shouldProcessTransferSuccessfully() throws Exception {
            TransferRequest request = TransferRequest.builder()
                    .fromUserId(3L)
                    .toUserId(4L)
                    .amount(new BigDecimal("300.00"))
                    .paymentMethod("IMPS")
                    .referenceNumber("TXN789")
                    .remarks("Payment")
                    .build();

            TransactionResponseDTO response = TransactionResponseDTO.builder()
                    .transactionId(UUID.randomUUID())
                    .transactionType("TRANSFER")
                    .status("COMPLETED")
                    .amount(new BigDecimal("300.00"))
                    .createdByUserId(1L)
                    .createdAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();

            when(walletService.processTransfer(any(TransferRequest.class), anyLong()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/wallets/transfer")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Transfer completed successfully"))
                    .andExpect(jsonPath("$.data.transactionType").value("TRANSFER"));

            verify(walletService).processTransfer(any(TransferRequest.class), anyLong());
        }

        @Test
        @DisplayName("Should return 400 for invalid transfer")
        void shouldReturn400ForInvalidTransfer() throws Exception {
            TransferRequest request = TransferRequest.builder()
                    .fromUserId(3L)
                    .toUserId(3L)
                    .amount(new BigDecimal("300.00"))
                    .build();

            when(walletService.processTransfer(any(TransferRequest.class), anyLong()))
                    .thenThrow(new InvalidTransactionException("Cannot transfer to the same wallet"));

            mockMvc.perform(post("/api/v1/wallets/transfer")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Cannot transfer to the same wallet")));
        }
    }

    @Nested
    @DisplayName("Wallet Statement Tests")
    class WalletStatementTests {

        @Test
        @DisplayName("Should get wallet statement successfully")
        void shouldGetWalletStatementSuccessfully() throws Exception {
            LocalDateTime fromDate = LocalDateTime.now().minusDays(30);
            LocalDateTime toDate = LocalDateTime.now();

            WalletStatementDTO statementDTO = WalletStatementDTO.builder()
                    .walletId(1L)
                    .userId(3L)
                    .currencyCode("INR")
                    .openingBalance(BigDecimal.ZERO)
                    .closingBalance(new BigDecimal("1000.00"))
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .totalEntries(5)
                    .build();

            when(walletService.getWalletStatement(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(statementDTO);

            mockMvc.perform(get("/api/v1/wallets/statement/3")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("fromDate", fromDate.toString())
                            .param("toDate", toDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.walletId").value(1))
                    .andExpect(jsonPath("$.data.totalEntries").value(5));

            verify(walletService).getWalletStatement(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Wallet History Tests")
    class WalletHistoryTests {

        @Test
        @DisplayName("Should get paginated wallet history")
        void shouldGetPaginatedWalletHistory() throws Exception {
            TransactionEntryDTO entryDTO = TransactionEntryDTO.builder()
                    .entryId(1L)
                    .transactionId(UUID.randomUUID())
                    .walletId(1L)
                    .userId(3L)
                    .entryType("CREDIT")
                    .amount(new BigDecimal("1000.00"))
                    .balanceAfter(new BigDecimal("1000.00"))
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<TransactionEntryDTO> page = new PageImpl<>(Collections.singletonList(entryDTO));

            when(walletService.getWalletHistory(anyLong(), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/wallets/history/3")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].entryType").value("CREDIT"));

            verify(walletService).getWalletHistory(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Suspend and Activate Tests")
    class SuspendActivateTests {

        @Test
        @DisplayName("Should suspend wallet successfully")
        void shouldSuspendWalletSuccessfully() throws Exception {
            testWalletDTO.setStatus("SUSPENDED");

            when(walletService.suspendWallet(anyLong(), anyLong()))
                    .thenReturn(testWalletDTO);

            mockMvc.perform(put("/api/v1/wallets/suspend/3")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Wallet suspended successfully"))
                    .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

            verify(walletService).suspendWallet(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should activate wallet successfully")
        void shouldActivateWalletSuccessfully() throws Exception {
            when(walletService.activateWallet(anyLong(), anyLong()))
                    .thenReturn(testWalletDTO);

            mockMvc.perform(put("/api/v1/wallets/activate/3")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Wallet activated successfully"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            verify(walletService).activateWallet(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should fail to suspend with user role")
        void shouldFailToSuspendWithUserRole() throws Exception {
            mockMvc.perform(put("/api/v1/wallets/suspend/3")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());

            verify(walletService, never()).suspendWallet(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("Get All Wallets Tests")
    class GetAllWalletsTests {

        @Test
        @DisplayName("Should get all wallets successfully")
        void shouldGetAllWalletsSuccessfully() throws Exception {
            List<WalletDTO> wallets = Arrays.asList(testWalletDTO);

            when(walletService.getAllWallets()).thenReturn(wallets);

            mockMvc.perform(get("/api/v1/wallets")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].walletId").value(1));

            verify(walletService).getAllWallets();
        }

        @Test
        @DisplayName("Should fail to get all wallets with user role")
        void shouldFailToGetAllWalletsWithUserRole() throws Exception {
            mockMvc.perform(get("/api/v1/wallets")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());

            verify(walletService, never()).getAllWallets();
        }
    }
}
