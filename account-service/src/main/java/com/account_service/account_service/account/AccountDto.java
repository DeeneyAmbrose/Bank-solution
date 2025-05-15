package com.account_service.account_service.account;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String iban;
    private String bicSwift;
    private Long customerId;
}
