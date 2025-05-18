package com.account_service.account_service.accounts;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.account_service.account_service.account.*;
import com.account_service.account_service.utilities.EntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
public class AccountServiceTests {
    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountsService accountsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Inject the URL property manually (simulate @Value)
        ReflectionTestUtils.setField(accountsService, "customerServiceUrl", "http://test-url/");
    }

    @Test
    void createAccount_successfulCreation() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId("1");  // customerId as String now
        request.setBicSwift("BIC456");

        // Mock customer service call success
        EntityResponse<CustomerDto> customerResponseBody = new EntityResponse<>();
        customerResponseBody.setStatusCode(200);
        customerResponseBody.setPayload(new CustomerDto());
        ResponseEntity<EntityResponse<CustomerDto>> customerResponseEntity =
                new ResponseEntity<>(customerResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(customerResponseEntity);

        // Mock findLastAccountNumberByPrefix (to generate next account number)
        when(accountsRepository.findLastAccountNumberByPrefix(anyString())).thenReturn(null);

        // Since IBAN is generated inside service, mock findByIban to return empty Optional
        when(accountsRepository.findByIban(anyString())).thenReturn(Optional.empty());

        // Mock save method to return the Account with an ID set as String
        doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setAccountId("1100192500001");  // accountId is String
            return account;
        }).when(accountsRepository).save(any(Account.class));

        EntityResponse<Account> response = accountsService.createAccount(request);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Account created successfully", response.getMessage());
        assertNotNull(response.getPayload());
        assertEquals(request.getCustomerId(), response.getPayload().getCustomerId());
        assertNotNull(response.getPayload().getIban());
        assertNotNull(response.getPayload().getAccountId());

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
        verify(accountsRepository).findByIban(anyString());
        verify(accountsRepository).save(any(Account.class));
    }

    @Test
    void createAccount_customerNotFound() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId("1");

        ResponseEntity<EntityResponse<CustomerDto>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        EntityResponse<Account> response = accountsService.createAccount(request);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());

        verify(accountsRepository, never()).findByIban(any());
        verify(accountsRepository, never()).save(any());
    }

    @Test
    void createAccount_ibanAlreadyExists() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId("1");

        EntityResponse<CustomerDto> customerResponseBody = new EntityResponse<>();
        customerResponseBody.setStatusCode(200);
        customerResponseBody.setPayload(new CustomerDto());
        ResponseEntity<EntityResponse<CustomerDto>> customerResponseEntity =
                new ResponseEntity<>(customerResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(customerResponseEntity);

        // Mock findLastAccountNumberByPrefix returns some last account number for sequence increment
        when(accountsRepository.findLastAccountNumberByPrefix(anyString())).thenReturn("1100192500001");

        // Mock that IBAN exists
        when(accountsRepository.findByIban(anyString())).thenReturn(Optional.of(new Account()));

        EntityResponse<Account> response = accountsService.createAccount(request);

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode());
        assertEquals("IBAN already exists", response.getMessage());

        verify(accountsRepository).findByIban(anyString());
        verify(accountsRepository, never()).save(any());
    }

    @Test
    void createAccount_exceptionHandling() {
        AccountRequest request = new AccountRequest();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("RestTemplate error"));

        EntityResponse<Account> response = accountsService.createAccount(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }

    // -- fetchAccountById tests --

    @Test
    void fetchAccountById_foundAndNotDeleted() {
        Account account = new Account();
        account.setAccountId("1100192500001"); // accountId is String
        account.setDeletedFlag("N");
        account.setBicSwift("BIC123");
        account.setCustomerId("1");            // customerId is String

        when(accountsRepository.findByAccountId("1100192500001")).thenReturn(Optional.of(account));

        EntityResponse<AccountDto> response = accountsService.fetchAccountById("1100192500001");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Account fetched successfully", response.getMessage());

        AccountDto dto = response.getPayload();
        assertNotNull(dto);
        assertEquals(account.getBicSwift(), dto.getBicSwift());
        assertEquals(account.getCustomerId(), dto.getCustomerId());
        assertEquals(account.getAccountId(), dto.getAccountId());
    }

    @Test
    void fetchAccountById_foundButDeleted() {
        Account account = new Account();
        account.setDeletedFlag("Y");

        when(accountsRepository.findByAccountId("1100192500001")).thenReturn(Optional.of(account));

        EntityResponse<AccountDto> response = accountsService.fetchAccountById("1100192500001");

        assertEquals(HttpStatus.GONE.value(), response.getStatusCode());
        assertEquals("Account has been deleted", response.getMessage());
        assertNull(response.getPayload());
    }

    @Test
    void fetchAccountById_notFound() {
        when(accountsRepository.findByAccountId("nonexistent")).thenReturn(Optional.empty());

        EntityResponse<AccountDto> response = accountsService.fetchAccountById("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Account not found", response.getMessage());
    }

    @Test
    void fetchAccountById_exception() {
        when(accountsRepository.findByAccountId(anyString())).thenThrow(new RuntimeException("DB error"));

        EntityResponse<AccountDto> response = accountsService.fetchAccountById("1100192500001");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }


    // -- editAccount tests --

    @Test
    void editAccount_success() {
        Account existingAccount = new Account();
        existingAccount.setDeletedFlag("N");
        existingAccount.setId(1L);
        existingAccount.setBicSwift("OLD_BIC");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        doAnswer(invocation -> invocation.getArgument(0)).when(accountsRepository).save(any(Account.class));

        AccountDto dto = new AccountDto();
        dto.setBicSwift("NEW_BIC");

        EntityResponse<Account> response = accountsService.editAccount(dto, 1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Account updated successfully", response.getMessage());
        assertEquals("NEW_BIC", response.getPayload().getBicSwift());
    }

    @Test
    void editAccount_cannotEditDeleted() {
        Account existingAccount = new Account();
        existingAccount.setDeletedFlag("Y");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(existingAccount));

        AccountDto dto = new AccountDto();
        dto.setBicSwift("NEW_BIC");

        EntityResponse<Account> response = accountsService.editAccount(dto, 1L);

        assertEquals(HttpStatus.GONE.value(), response.getStatusCode());
        assertEquals("Cannot edit a deleted account", response.getMessage());

        verify(accountsRepository, never()).save(any());
    }

    @Test
    void editAccount_notFound() {
        when(accountsRepository.findById(1L)).thenReturn(Optional.empty());

        AccountDto dto = new AccountDto();
        dto.setBicSwift("NEW_BIC");

        EntityResponse<Account> response = accountsService.editAccount(dto, 1L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Account not found", response.getMessage());

        verify(accountsRepository, never()).save(any());
    }

    @Test
    void editAccount_exception() {
        when(accountsRepository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));

        AccountDto dto = new AccountDto();
        dto.setBicSwift("NEW_BIC");

        EntityResponse<Account> response = accountsService.editAccount(dto, 1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }
    // -- deleteAccount tests --

    @Test
    void deleteAccount_success() {
        Account account = new Account();
        account.setDeletedFlag("N");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(account));
        doAnswer(invocation -> invocation.getArgument(0)).when(accountsRepository).save(any(Account.class));

        EntityResponse<Account> response = accountsService.deleteAccount(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Account deleted successfully (soft delete)", response.getMessage());
        assertEquals("Y", response.getPayload().getDeletedFlag());
    }

    @Test
    void deleteAccount_notFound() {
        when(accountsRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<Account> response = accountsService.deleteAccount(1L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Account not found", response.getMessage());
    }

    @Test
    void deleteAccount_exception() {
        when(accountsRepository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));

        EntityResponse<Account> response = accountsService.deleteAccount(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }


    // -- searchAccounts tests --

    @Test
    void searchAccounts_success() {
        AccountSearchRequest request = new AccountSearchRequest();
        request.setIban("IBAN123");
        request.setBicSwift("BIC456");
        request.setPage(0);
        request.setSize(10);

        Page<Account> mockedPage = new PageImpl<>(Collections.emptyList());

        when(accountsRepository.searchAccounts(
                eq(request.getIban()),
                eq(request.getBicSwift()),
                any(Pageable.class))
        ).thenReturn(mockedPage);

        EntityResponse<Page<Account>> response = accountsService.searchAccounts(request);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Accounts fetched successfully", response.getMessage());
        assertEquals(mockedPage, response.getPayload());
    }

    @Test
    void searchAccounts_exception() {
        AccountSearchRequest request = new AccountSearchRequest();

        when(accountsRepository.searchAccounts(any(), any(), any())).thenThrow(new RuntimeException("DB error"));

        EntityResponse<Page<Account>> response = accountsService.searchAccounts(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }
}
