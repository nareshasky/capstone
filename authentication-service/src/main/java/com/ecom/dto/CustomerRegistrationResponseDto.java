package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerRegistrationResponseDto {

    private int userId;
    private String username;
    private String email;
    private String role;
    private String message;
}

