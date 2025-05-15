package com.customer_service.customer_service.authentication.user;

import com.customer_service.authentication.enums.Status;
import lombok.Data;

@Data
public class UserDTO {
    Long userId;
    String username;
    String firstname;
    String lastname;
    String email;
    String role;

    String deletedFlag;
    String enabledFlag;
    Status status;

    String createdOn;
    String updatedOn;
}
