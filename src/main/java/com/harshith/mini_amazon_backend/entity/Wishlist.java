package com.harshith.mini_amazon_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mirrors the Cart/cart_item pattern: table name is "wishlist_item" (a row is
// one saved product for one user), with a DB-level unique constraint on
// (user_id, product_id) so duplicates are impossible even if application-level
// checks are ever bypassed (race conditions, direct DB access, future code
// changes, etc). Relying on application logic alone is not enough.
@Entity
@Table(
        name = "wishlist_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BUG FIX: this was previously annotated with @JoinColumn, which is only
    // valid on entity associations (@ManyToOne/@OneToOne/etc). userId is a
    // plain Long, not a relationship, so it needs a normal @Column - the old
    // code happened to work because Hibernate silently ignored the misapplied
    // annotation, but it was incorrect and misleading to read.
    @NotNull(message = "userId should not be empty")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

//Ask yourself:
//
//Is this field another entity object?
//
//Yes → Use a relationship annotation (@OneToOne, @OneToMany, @ManyToOne, or @ManyToMany) and, where applicable, @JoinColumn.
//        No (it's a simple value like String, int, BigDecimal, LocalDate, etc.) → Use @Column (or omit it if you don't need any customization, since JPA maps basic fields by default).
//
//This rule will be correct for almost all JPA entity mappings.