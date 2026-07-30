package com.harshith.mini_amazon_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User entity. Implements Spring Security's UserDetails directly instead of
 * introducing a separate wrapper class - this project has no reason (yet)
 * for the persisted user shape and the security-facing shape to diverge, so
 * a second mapping class today would be needless indirection.
 */
@Entity
// "user" is a reserved keyword in PostgreSQL (and several other databases),
// so Hibernate's default table name (the class name, lowercased) produces
// invalid DDL: `create table user (...)` fails with a syntax error. Mapping
// to "users" instead sidesteps the reserved word entirely.
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    // Email doubles as the username for login. unique = true is enforced at
    // the DB level - the @NotBlank/@Email checks on RegisterRequest handle
    // the "is this well-formed" case, but only a DB constraint can guarantee
    // no two rows ever end up with the same email under concurrent requests.
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    // Stores the BCrypt hash, never the raw password.
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}