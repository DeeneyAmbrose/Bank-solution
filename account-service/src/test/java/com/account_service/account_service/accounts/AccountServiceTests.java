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

    // -- createAccount tests --

    @Test
    void createAccount_successfulCreation() {
        AccountDto dto = new AccountDto();
        dto.setCustomerId(1L);
        dto.setIban("IBAN123");
        dto.setBicSwift("BIC456");

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

        // Mock IBAN not existing
        when(accountsRepository.findByIban(dto.getIban())).thenReturn(Optional.empty());

        // Mock saving account (no return needed for void)
        doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        }).when(accountsRepository).save(any(Account.class));

        EntityResponse<Account> response = accountsService.createAccount(dto);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Account created successfully", response.getMessage());
        assertNotNull(response.getPayload());
        assertEquals(dto.getIban(), response.getPayload().getIban());

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
        verify(accountsRepository).findByIban(dto.getIban());
        verify(accountsRepository).save(any(Account.class));
    }

    @Test
    void createAccount_customerNotFound() {
        AccountDto dto = new AccountDto();
        dto.setCustomerId(1L);
        dto.setIban("IBAN123");

        ResponseEntity<EntityResponse<CustomerDto>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        EntityResponse<Account> response = accountsService.createAccount(dto);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());

        verify(accountsRepository, never()).findByIban(any());
        verify(accountsRepository, never()).save(any());
    }

    @Test
    void createAccount_ibanAlreadyExists() {
        AccountDto dto = new AccountDto();
        dto.setCustomerId(1L);
        dto.setIban("IBAN123");

        EntityResponse<CustomerDto> customerResponseBody = new EntityResponse<>();
        customerResponseBody.setStatusCode(200);
        customerResponseBody.setPayload(new CustomerDto());
        ResponseEntity<EntityResponse<CustomerDto>> customerResponseEntity =
                new ResponseEntity<>(customerResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(customerResponseEntity);

        when(accountsRepository.findByIban(dto.getIban())).thenReturn(Optional.of(new Account()));

        EntityResponse<Account> response = accountsService.createAccount(dto);

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode());
        assertEquals("IBAN already exists", response.getMessage());

        verify(accountsRepository).findByIban(dto.getIban());
        verify(accountsRepository, never()).save(any());
    }

    @Test
    void createAccount_exceptionHandling() {
        AccountDto dto = new AccountDto();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("RestTemplate error"));

        EntityResponse<Account> response = accountsService.createAccount(dto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }


    // -- fetchAccountById tests --

    @Test
    void fetchAccountById_foundAndNotDeleted() {
        Account account = new Account();
        account.setId(1L);
        account.setDeletedFlag("N");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(account));

        EntityResponse<Account> response = accountsService.fetchAccountById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Account fetched successfully", response.getMessage());
        assertEquals(account, response.getPayload());
    }

    @Test
    void fetchAccountById_foundButDeleted() {
        Account account = new Account();
        account.setDeletedFlag("Y");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(account));

        EntityResponse<Account> response = accountsService.fetchAccountById(1L);

        assertEquals(HttpStatus.GONE.value(), response.getStatusCode());
        assertEquals("Account has been deleted", response.getMessage());
        assertNull(response.getPayload());
    }

    @Test
    void fetchAccountById_notFound() {
        when(accountsRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<Account> response = accountsService.fetchAccountById(1L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Account not found", response.getMessage());
    }

    @Test
    void fetchAccountById_exception() {
        when(accountsRepository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));

        EntityResponse<Account> response = accountsService.fetchAccountById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertTrue(response.getMessage().contains("Error:"));
    }


    // -- editAccount tests --

    @Test
    void editAccount_success() {
        Account existingAccount = new Account();
        existingAccount.setDeletedFlag("N");
        existingAccount.setId(1L);
        existingAccount.setIban("OLD_IBAN");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        doAnswer(invocation -> invocation.getArgument(0)).when(accountsRepository).save(any(Account.class));

        AccountDto dto = new AccountDto();
        dto.setIban("NEW_IBAN");
        dto.setBicSwift("NEW_BIC");

        EntityResponse<Account> response = accountsService.editAccount(dto, 1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Account updated successfully", response.getMessage());
        assertEquals("NEW_IBAN", response.getPayload().getIban());
        assertEquals("NEW_BIC", response.getPayload().getBicSwift());
    }

    @Test
    void editAccount_accountDeleted() {
        Account existingAccount = new Account();
        existingAccount.setDeletedFlag("Y");

        when(accountsRepository.findById(1L)).thenReturn(Optional.of(existingAccount));

        AccountDto dto = new AccountDto();

        EntityResponse<Account> response = accountsService.editAccount(dto, 1L);

        assertEquals(HttpStatus.GONE.value(), response.getStatusCode());
        assertEquals("Cannot edit a deleted account", response.getMessage());
    }

    @Test
    void editAccount_accountNotFound() {
        when(accountsRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<Account> response = accountsService.editAccount(new AccountDto(), 1L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Account not found", response.getMessage());
    }

    @Test
    void editAccount_exception() {
        when(accountsRepository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));

        EntityResponse<Account> response = accountsService.editAccount(new AccountDto(), 1L);

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
