package com.harshith.mini_amazon_backend.entity;

/**
 * A minimal role model - just enough for Spring Security's authorization
 * checks to have something to check against. Not adding admin-specific
 * endpoints or permission logic today; that's future work, not part of
 * today's task.
 */
public enum Role {
    USER,
    ADMIN
}