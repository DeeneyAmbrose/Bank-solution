package com.account_service.account_service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final  AccountsService accountsService;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody AccountDto accountDto) {
        var response= accountsService.createAccount(accountDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @GetMapping
    public ResponseEntity<?> fetchAccountById(@RequestParam Long accountId ) {
        var response= accountsService.fetchAccountById(accountId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @PutMapping
    public ResponseEntity<?> editAccount(@RequestBody AccountDto accountDto,@RequestParam Long accountId) {
        var response= accountsService.editAccount(accountDto, accountId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

   @DeleteMapping
    public ResponseEntity<?> deleteAccount(@RequestParam Long accountId) {
        var response= accountsService.deleteAccount( accountId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAccounts(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String bicSwift,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AccountSearchRequest request = new AccountSearchRequest();
        request.setIban(iban);
        request.setBicSwift(bicSwift);
        request.setPage(page);
        request.setSize(size);

        var response = accountsService.searchAccounts(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
