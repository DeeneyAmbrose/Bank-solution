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
        AccountRequest request = new AccountRequest();  // changed from AccountDto
        EntityResponse<Account> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(201);
        responseFromService.setMessage("Created");
        responseFromService.setPayload(new Account());

        when(accountsService.createAccount(request)).thenReturn(responseFromService);

        ResponseEntity<?> response = accountController.createAccount(request);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());
        verify(accountsService).createAccount(request);
    }

    @Test
    void fetchAccountById_shouldReturnResponseFromService() {
        String accountId = "1";

        AccountDto accountDto = new AccountDto();
        accountDto.setAccountId(accountId);
        accountDto.setBicSwift("BIC123");
        accountDto.setCustomerId("CUST001");

        EntityResponse<AccountDto> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Found");
        responseFromService.setPayload(accountDto);  // Use AccountDto here

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

        Page<Account> mockedPage = new PageImpl<>(Collections.emptyList());

        EntityResponse<Page<Account>> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Search results");
        responseFromService.setPayload(mockedPage);

        when(accountsService.searchAccounts(any(AccountSearchRequest.class))).thenReturn(responseFromService);

        ResponseEntity<?> response = accountController.searchAccounts(iban, bicSwift, page, size);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());

        ArgumentCaptor<AccountSearchRequest> captor = ArgumentCaptor.forClass(AccountSearchRequest.class);
        verify(accountsService).searchAccounts(captor.capture());

        AccountSearchRequest captured = captor.getValue();
        assertEquals(iban, captured.getIban());
        assertEquals(bicSwift, captured.getBicSwift());
        assertEquals(page, captured.getPage());
        assertEquals(size, captured.getSize());
    }

}
