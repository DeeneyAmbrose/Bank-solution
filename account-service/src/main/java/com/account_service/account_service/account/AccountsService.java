package com.account_service.account_service.account;

import com.account_service.account_service.utilities.EntityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountsService {
    private final AccountsRepository accountsRepository;
    private final RestTemplate restTemplate;

    @Value("${customer-service.url}")
    private String customerServiceUrl;



    public EntityResponse<?> createAccount(AccountDto dto) {
        EntityResponse<Account> response = new EntityResponse<>();

        try {
            // Call customer-service to validate customerId
            ResponseEntity<EntityResponse<CustomerDto>> customerResponse =
                    restTemplate.exchange(
                            customerServiceUrl + "customers?customerId=" + dto.getCustomerId(),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            if (!customerResponse.getStatusCode().is2xxSuccessful() || customerResponse.getBody() == null) {
                response.setMessage("Customer not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }

            // ✅ Check if IBAN already exists
            Optional<Account> existingAccount = accountsRepository.findByIban(dto.getIban());
            if (existingAccount.isPresent()) {
                response.setMessage("IBAN already exists");
                response.setStatusCode(HttpStatus.CONFLICT.value());
                return response;
            }

            // Proceed to save the account
            Account account = new Account();
            account.setCustomerId(dto.getCustomerId());
            account.setIban(dto.getIban());
            account.setBicSwift(dto.getBicSwift());
            accountsRepository.save(account);

            response.setPayload(account);
            response.setMessage("Account created successfully");
            response.setStatusCode(HttpStatus.CREATED.value());
        } catch (Exception ex) {
            response.setMessage("Error: " + ex.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    public EntityResponse<?> fetchAccountById(Long accountId) {
        EntityResponse<Account> response = new EntityResponse<>();

        try {
            Optional<Account> optionalAccount = accountsRepository.findById(accountId);

            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();

                if ("Y".equalsIgnoreCase(account.getDeletedFlag())) {
                    response.setMessage("Account has been deleted");
                    response.setStatusCode(HttpStatus.GONE.value()); // 410 Gone
                } else {
                    response.setPayload(account);
                    response.setMessage("Account fetched successfully");
                    response.setStatusCode(HttpStatus.OK.value());
                }
            } else {
                response.setMessage("Account not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    public EntityResponse<Account> editAccount(AccountDto accountDto, Long accountId) {
        EntityResponse<Account> response = new EntityResponse<>();

        try {
            Optional<Account> optionalAccount = accountsRepository.findById(accountId);

            if (optionalAccount.isPresent()) {
                Account existingAccount = optionalAccount.get();

                // Check if the account has been marked as deleted
                if ("Y".equalsIgnoreCase(existingAccount.getDeletedFlag())) {
                    response.setMessage("Cannot edit a deleted account");
                    response.setStatusCode(HttpStatus.GONE.value()); // 410 Gone
                    return response;
                }

                existingAccount.setIban(accountDto.getIban());
                existingAccount.setBicSwift(accountDto.getBicSwift());

                accountsRepository.save(existingAccount);

                response.setPayload(existingAccount);
                response.setMessage("Account updated successfully");
                response.setStatusCode(HttpStatus.OK.value());
            } else {
                response.setMessage("Account not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    public EntityResponse<?> deleteAccount(Long accountId) {
        EntityResponse<Account> response = new EntityResponse<>();
        try {
            Optional<Account> optionalAccount = accountsRepository.findById(accountId);
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                account.setDeletedFlag("Y");
                accountsRepository.save(account);

                response.setPayload(account);
                response.setMessage("Account deleted successfully (soft delete)");
                response.setStatusCode(HttpStatus.OK.value());
            } else {
                response.setMessage("Account not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public EntityResponse<?> searchAccounts(AccountSearchRequest request) {
        EntityResponse<Page<Account>> response = new EntityResponse<>();
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<Account> result = accountsRepository.searchAccounts(
                    request.getIban(),
                    request.getBicSwift(),
                    pageable
            );
            response.setPayload(result);
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Accounts fetched successfully");
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

}
