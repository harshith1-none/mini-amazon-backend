package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserId(Long userId);

    // "ProductId" here resolves through the Cart -> Product relationship
    // (product.id) automatically via Spring Data's nested-property query
    // derivation - no @Query needed.
    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);
}