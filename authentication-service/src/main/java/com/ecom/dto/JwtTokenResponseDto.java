package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtTokenResponseDto {

    private String accessToken;
    private String tokenType;   // Bearer
    private Long expiresIn;     // seconds
    private int userId;
    private String username;
    private String role;
}

