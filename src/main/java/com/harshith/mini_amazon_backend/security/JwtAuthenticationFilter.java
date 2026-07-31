package com.harshith.mini_amazon_backend.security;

import com.harshith.mini_amazon_backend.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // BUG FIX: was "Autherisation" (misspelled, not a real HTTP header).
    // The real header is "Authorization" - with the old value this filter
    // never found a token on any request.
    private static final String AUTH_HEADER = "Authorization";

    // BUG FIX: needs the trailing space. "Bearer <token>".startsWith("Bearer")
    // is true even without it, but substring(BEARER_PREFIX.length()) would
    // then start cutting right after "Bearer", keeping the space before the
    // token and producing " <token>" - which fails JWT parsing.
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        // BUG FIX: was missing `return` here. Without it, every request
        // with no/invalid Authorization header fell through to
        // authHeader.substring(...) on a null authHeader, throwing an
        // uncaught NullPointerException on every unauthenticated request
        // (including public endpoints like GET /api/products).
        if(authHeader==null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            String email = jwtService.extractUsername(token);
            if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if(jwtService.isTokenValid(token,userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,null,userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }
        } catch(JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request,response);
    }
}