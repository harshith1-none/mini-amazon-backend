package com.harshith.mini_amazon_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Runs when a request hits an endpoint that requires authentication and
// JwtAuthenticationFilter never set an Authentication in the
// SecurityContext - i.e. the Authorization header was missing, malformed,
// or the token was invalid/expired.
//
// Without this bean, Spring Security falls back to Http403ForbiddenEntryPoint
// (this app has no httpBasic()/formLogin() to register their own entry
// point), which returns a bare 403 for EVERY unauthenticated request -
// indistinguishable from a genuine "logged in, but not allowed" 403. 401 is
// the correct status for "you're not authenticated at all", and it's what
// the frontend's auto-logout-on-401 handling (httpClient.js) listens for.
//
// Note: GlobalExceptionHandler's @ExceptionHandler(AuthenticationException.class)
// does NOT catch this case - @RestControllerAdvice only intercepts
// exceptions thrown inside controller/service code reached through the
// DispatcherServlet. This entry point runs earlier, from
// ExceptionTranslationFilter in the security filter chain, before a
// controller is ever invoked.
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Missing or invalid authentication token");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}