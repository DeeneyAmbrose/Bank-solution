package com.customer_service.customer_service.utilities;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class EntityResponse <T> {
    private String message;
    private Integer statusCode = HttpStatus.OK.value();
    private T payload;
}
