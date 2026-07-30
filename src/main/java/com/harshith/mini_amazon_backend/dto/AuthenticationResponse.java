package com.harshith.mini_amazon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String token;

    // Lets the frontend build the "Authorization: Bearer <token>" header
    // without hardcoding the scheme name on its side.
    private String tokenType = "Bearer";

    private String name;

    private String email;

    public AuthenticationResponse(String token, String name, String email) {
        this.token = token;
        this.tokenType = "Bearer";
        this.name = name;
        this.email = email;
    }
}