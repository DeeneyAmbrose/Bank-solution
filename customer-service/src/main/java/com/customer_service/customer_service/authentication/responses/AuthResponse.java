package com.customer_service.customer_service.authentication.responses;

import com.customer_service.customer_service.authentication.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String token;
    private Role role;
}
