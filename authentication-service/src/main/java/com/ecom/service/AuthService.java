package com.ecom.service;


import com.ecom.dto.CustomerDto;
import com.ecom.dto.CustomerRegistrationResponseDto;
import com.ecom.entity.CustomerCredential;
import com.ecom.exception.CustomerAlreadyExistException;
import com.ecom.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public CustomerRegistrationResponseDto saveCustomer(CustomerDto credential) {
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        CustomerCredential customerCredential = toCustomerCredentialEntity(credential);
        Optional<CustomerCredential> custFetchResp = repository.findByUsernameOrEmail(customerCredential.getUsername(),customerCredential.getEmail());
        if (custFetchResp.isPresent()) {
            throw new CustomerAlreadyExistException("Customer already exist try with different username");
        }else{
        CustomerCredential savedEntity = repository.save(customerCredential);
        return new CustomerRegistrationResponseDto(
                savedEntity.getId(),
                savedEntity.getUsername(),
                savedEntity.getEmail(),
                savedEntity.getRole(),
                "User added to the system"
        );
    }
    }

    public String generateToken(String username, String role,int id) {

        return jwtService.generateToken(username,role,id);
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }

    //CustomerDetailsDto to Entity
    public static CustomerCredential toCustomerCredentialEntity(CustomerDto dto) {

        CustomerCredential entity = new CustomerCredential();
        entity.setUsername(dto.getUsername());   // username → name
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setRole(dto.getRole());

        return entity;
    }

    //CustomerCredential entity to DTO
    public static CustomerDto toDto(CustomerCredential entity) {

        CustomerDto dto = new CustomerDto();
        dto.setUsername(entity.getUsername());   // name → username
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole());
        dto.setPassword(null);

        return dto;
    }

}
