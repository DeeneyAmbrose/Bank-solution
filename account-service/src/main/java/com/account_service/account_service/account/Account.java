package com.account_service.account_service.account;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String iban;
    private String bicSwift;
    private String customerId;
    private String accountId;
    private String deletedFlag ="N";
}
