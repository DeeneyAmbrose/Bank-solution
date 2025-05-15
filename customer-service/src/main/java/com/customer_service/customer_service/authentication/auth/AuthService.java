package com.customer_service.customer_service.authentication.auth;

import com.customer_service.authentication.requests.AuthRequest;


import com.customer_service.customer_service.authentication.jwt.JwtService;
import com.customer_service.customer_service.authentication.responses.AuthResponse;
import com.customer_service.customer_service.authentication.user.CurrentContext;
import com.customer_service.customer_service.authentication.user.User;
import com.customer_service.customer_service.authentication.user.UserRepository;
import com.customer_service.customer_service.configurations.threads.CurrentUserContext;
import com.customer_service.customer_service.utilities.EntityResponse;
import com.customer_service.customer_service.utilities.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Autowired
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;


    public EntityResponse<AuthResponse> authenticate(AuthRequest request) {
        EntityResponse<AuthResponse> response = new EntityResponse<>();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = new User();

            if(userRepository.findByUsername(request.getUsername()).isPresent()) {
                user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                        () -> new ResourceNotFoundException("User with that username not found")
                );
            } else if (userRepository.findByEmail(request.getUsername()).isPresent()) {
                user = userRepository.findByEmail(request.getUsername()).orElseThrow(
                        () -> new ResourceNotFoundException("User with that email not found")
                );
                user.setUsername(user.getEmail());
            }

            CurrentContext context = new CurrentContext();
            context.setUserId(user.getId());
            CurrentUserContext.setCurrentContext(context);

            var jwtToken = jwtService.generateJwtToken(user);
            var authResponse = AuthResponse.builder()
                    .username(user.getUsername())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .token(jwtToken)
                    .role(user.getRole())
                    .build();

            response.setMessage("success");
            response.setPayload(authResponse);

        } catch (Exception e) {
            log.error(e.getMessage());
            response.setMessage(e.getMessage());
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        }
        return response;
    }
}
