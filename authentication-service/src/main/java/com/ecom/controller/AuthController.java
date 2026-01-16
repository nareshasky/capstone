package com.ecom.controller;


import com.ecom.config.CustomerUserDetails;
import com.ecom.dto.CustomerDto;
import com.ecom.dto.CustomerRegistrationResponseDto;
import com.ecom.dto.JwtTokenResponseDto;
import com.ecom.dto.LoginTokenDto;
import com.ecom.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RefreshScope
@Slf4j
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponseDto> addNewUser(
            @Validated @RequestBody CustomerDto customerDetailsDto) {
        log.info("Received registration request for username: {}",
                customerDetailsDto.getUsername());
        CustomerRegistrationResponseDto response =
                service.saveCustomer(customerDetailsDto);
        log.info("User registration successful for username: {}",
                customerDetailsDto.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtTokenResponseDto> getToken(
            @RequestBody LoginTokenDto authRequest) {
        log.info("Token request received for username: {}",
                authRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        CustomerUserDetails userDetails =
                (CustomerUserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
//                .replace("ROLE_", "");

        int userId = userDetails.getId();
        log.debug("Authentication successful for userId: {}, role: {}",
                userId, role);
        String token = service.generateToken(
                authRequest.getUsername(),
                role,
                userId
        );
        log.info("JWT token generated successfully for username: {}",
                authRequest.getUsername());
        JwtTokenResponseDto response = new JwtTokenResponseDto(
                token,
                "Bearer",
                1800L, // 30 minutes (should match JWT exp)
                userId,
                authRequest.getUsername(),
                role
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/validate")
    public String validateToken() {//@RequestParam("token") String token
        //service.validateToken(token);
        log.info("Token validation endpoint called");
        return "Token is valid";
    }

    @Value("${spring.datasource.password}")
    private String jwtSecret;

    @GetMapping("/secret")
    public String getSecret() {
        log.warn("Accessing secret value via /secret endpoint");
        return jwtSecret;
    }
}
