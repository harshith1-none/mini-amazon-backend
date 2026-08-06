package com.harshith.mini_amazon_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Runs when a request IS authenticated (a valid JWT was presented) but the
// user doesn't have the required role - e.g. a non-admin hitting
// PATCH /api/orders/{id}/status, which is hasRole("ADMIN") in
// SecurityConfig. This is the genuine "logged in, not allowed" case that
// should stay 403 and must NOT trigger the frontend's auto-logout-on-401.
// Without this bean, Spring Security's default AccessDeniedHandler returns
// a bare 403 with no JSON body - inconsistent with every other error
// response in this API (see GlobalExceptionHandler).
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("message", "You do not have permission to perform this action");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}