package com.account_service.account_service.account;

import lombok.Data;

@Data
public class AccountRequest {
    private String bicSwift;
    private String customerId;
}
