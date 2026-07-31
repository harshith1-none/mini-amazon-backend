package com.harshith.mini_amazon_backend.security;

import com.harshith.mini_amazon_backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently authenticated User entity from Spring Security's
 * context.
 *
 * CustomUserDetailsService loads the User entity itself as the UserDetails
 * principal (User implements UserDetails directly), so the principal that
 * JwtAuthenticationFilter puts into the SecurityContext already IS the
 * managed User - no extra repository lookup needed here.
 */
@Component
public class CurrentUserProvider {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("No authenticated user found in security context");
        }

        return user;
    }
}