package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // NAMING FIX: parameter renamed from "defaultUserId" to "userId". This
    // repository method has nothing to do with a "default" user - the
    // repository layer shouldn't know or care that the service layer
    // currently hardcodes DEFAULT_USER_ID = 1L. The old name leaked a
    // service-layer concept into the repository's public API.
    List<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}