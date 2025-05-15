package com.card_service.card_service.cards;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String iban;
    private String bicSwift;
    private Long customerId;
}
