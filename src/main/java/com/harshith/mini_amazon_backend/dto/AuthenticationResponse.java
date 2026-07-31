package com.harshith.mini_amazon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BUG FIX: the previous version had a `password` field serialized straight
 * into the login/register JSON response, and a hand-written 3-arg
 * constructor whose parameter names (token, email, password) didn't match
 * how it was actually called in AuthService - which passed
 * (token, user.getName(), user.getEmail()). That silently put the user's
 * NAME into the "email" field and their EMAIL into the "password" field of
 * every auth response. Replaced with a plain @AllArgsConstructor over the
 * correct 4 fields (no password field at all) so there's no hand-written
 * constructor left to drift out of sync with its callers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String tokenType;
    private String name;
    private String email;
}