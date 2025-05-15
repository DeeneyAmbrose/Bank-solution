package com.customer_service.customer_service.customer;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class CustomerDto {
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String otherName;
}
