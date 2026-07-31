package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Day 7: was findByUserId(Long) - see CartRepository for the same fix
    // and reasoning.
    List<Wishlist> findByUser(User user);

    Optional<Wishlist> findByUserAndProductId(User user, Long productId);

    boolean existsByUserAndProductId(User user, Long productId);
}