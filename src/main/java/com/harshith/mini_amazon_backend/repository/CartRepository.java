package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Cart;
import com.harshith.mini_amazon_backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Day 7: was findByUserId(Long). Now that Cart.user is a real @ManyToOne
    // relationship, Spring Data derives the query by navigating the
    // association (still generates ... WHERE user_id = ? under the hood) -
    // no @Query needed, and the method signature now matches what the
    // entity actually looks like.
    List<Cart> findByUser(User user);

    Optional<Cart> findByUserAndProductId(User user, Long productId);

    void deleteByUser(User user);
}