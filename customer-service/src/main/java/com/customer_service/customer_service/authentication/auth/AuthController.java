package com.customer_service.customer_service.authentication.auth;


import com.customer_service.authentication.requests.AuthRequest;

import com.customer_service.customer_service.authentication.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;


    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest authRequest) {
        var result = authService.authenticate(authRequest);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }
}
