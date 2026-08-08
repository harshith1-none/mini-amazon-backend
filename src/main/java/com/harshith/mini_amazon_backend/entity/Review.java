package com.harshith.mini_amazon_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Same pattern as Cart/Wishlist: a DB-level unique constraint on
// (user_id, product_id) makes "one review per user per product" impossible
// to violate even under a race condition (two near-simultaneous requests
// both passing the application-level existsByUserAndProductId check before
// either has saved). Relying on the service-layer check alone is not
// enough - see WishlistService's javadoc-style comments for the same
// reasoning applied there.
//
// Deliberately NOT adding a bidirectional List<Review> back on User or
// Product. Order/OrderItem needed @ToString.Exclude / @EqualsAndHashCode.Exclude
// to break a toString()/equals() recursion because OrderItem holds a
// @ManyToOne back to the exact Order it belongs to AND Order holds the
// reverse @OneToMany. Review only ever points outward (to one User, one
// Product) with no collection pointing back, so that cycle simply doesn't
// exist here - keeping it unidirectional avoids needing those exclusions
// at all, and matches how Cart/Wishlist already relate to User and Product.
@Entity
@Table(
        name = "review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    @Column(nullable = false)
    private int rating;

    @NotBlank(message = "Comment must not be blank")
    @Column(length = 1000, nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Set once, server-side, at insert time - same @PrePersist pattern as
    // Order.orderDate. updatable = false means even if some future code
    // accidentally calls setCreatedAt() on an existing row, Hibernate will
    // not include the column in the UPDATE statement.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}