package com.account_service.account_service.accounts;

import com.account_service.account_service.account.*;
import com.account_service.account_service.utilities.EntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountControllerTests {
    @Mock
    private AccountsService accountsService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_shouldReturnResponseFromService() {
        AccountDto dto = new AccountDto();
        EntityResponse<Account> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(201);
        responseFromService.setMessage("Created");
        responseFromService.setPayload(new Account());

        when(accountsService.createAccount(dto)).thenReturn(responseFromService);

        ResponseEntity<?> response = accountController.createAccount(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());
        verify(accountsService).createAccount(dto);
    }

    @Test
    void fetchAccountById_shouldReturnResponseFromService() {
        Long accountId = 1L;
        EntityResponse<Account> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Found");
        responseFromService.setPayload(new Account());

        when(accountsService.fetchAccountById(accountId)).thenReturn(responseFromService);

        ResponseEntity<?> response = accountController.fetchAccountById(accountId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());
        verify(accountsService).fetchAccountById(accountId);
    }

    @Test
    void editAccount_shouldReturnResponseFromService() {
        Long accountId = 1L;
        AccountDto dto = new AccountDto();
        EntityResponse<Account> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Updated");
        responseFromService.setPayload(new Account());

        when(accountsService.editAccount(dto, accountId)).thenReturn(responseFromService);

        ResponseEntity<?> response = accountController.editAccount(dto, accountId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());
        verify(accountsService).editAccount(dto, accountId);
    }

    @Test
    void deleteAccount_shouldReturnResponseFromService() {
        Long accountId = 1L;
        EntityResponse<Account> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Deleted");
        responseFromService.setPayload(new Account());

        when(accountsService.deleteAccount(accountId)).thenReturn(responseFromService);

        ResponseEntity<?> response = accountController.deleteAccount(accountId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());
        verify(accountsService).deleteAccount(accountId);
    }

    @Test
    void searchAccounts_shouldReturnResponseFromService() {
        String iban = "IBAN123";
        String bicSwift = "BIC456";
        int page = 1;
        int size = 10;

        // Mock the Page<Account>
        Page<Account> mockedPage = new PageImpl<>(Collections.emptyList());

        // Prepare the response
        EntityResponse<Page<Account>> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Search results");
        responseFromService.setPayload(mockedPage);

        // Mock service behavior
        when(accountsService.searchAccounts(any(AccountSearchRequest.class))).thenReturn(responseFromService);

        // Call the controller method
        ResponseEntity<?> response = accountController.searchAccounts(iban, bicSwift, page, size);

        // Validate response
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());

        // Capture and verify the request sent to service
        ArgumentCaptor<AccountSearchRequest> captor = ArgumentCaptor.forClass(AccountSearchRequest.class);
        verify(accountsService).searchAccounts(captor.capture());

        AccountSearchRequest captured = captor.getValue();
        assertEquals(iban, captured.getIban());
        assertEquals(bicSwift, captured.getBicSwift());
        assertEquals(page, captured.getPage());
        assertEquals(size, captured.getSize());
    }

}
