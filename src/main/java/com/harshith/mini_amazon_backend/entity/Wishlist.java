package com.harshith.mini_amazon_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    // Day 7: was a raw `Long userId` (previously even mis-annotated with
    // @JoinColumn, which only applies to real associations). Replaced with
    // an actual @ManyToOne relationship to User, same pattern as `product`
    // below and as Cart.user - this is what "add a relationship between
    // User and WishlistItem" means in JPA terms.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}