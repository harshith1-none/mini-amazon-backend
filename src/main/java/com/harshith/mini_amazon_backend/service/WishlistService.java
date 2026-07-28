package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.WishlistResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.Wishlist;
import com.harshith.mini_amazon_backend.exception.DuplicateWishlistItemException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.exception.WishlistItemNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    // Temporary stand-in until JWT auth exists - same pattern used by
    // CartServiceImpl.DEFAULT_USER_ID.
    private static final Long DEFAULT_USER_ID = 1L;

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public List<WishlistResponseDto> getWishlist() {
        return wishlistRepository.findByUserId(DEFAULT_USER_ID)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // BUG FIX: the previous version silently returned the existing item
    // when the product was already wishlisted, instead of rejecting the
    // request. That violates today's task requirement ("Prevent duplicate
    // wishlist entries" / "Handle exceptions ... duplicate item") - a
    // duplicate POST must fail loudly with 409 Conflict, not succeed
    // silently, or the caller has no way to know their request was a no-op.
    public WishlistResponseDto addItem(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (wishlistRepository.existsByUserIdAndProductId(DEFAULT_USER_ID, productId)) {
            throw new DuplicateWishlistItemException(productId);
        }

        Wishlist wishlistItem = new Wishlist();
        wishlistItem.setUserId(DEFAULT_USER_ID);
        wishlistItem.setProduct(product);

        return toDto(wishlistRepository.save(wishlistItem));
    }

    public void deleteItem(Long productId) {
        Wishlist wishlistItem = getWishlistItemOrThrow(productId);
        wishlistRepository.delete(wishlistItem);
    }

    private Wishlist getWishlistItemOrThrow(Long productId) {
        return wishlistRepository.findByUserIdAndProductId(DEFAULT_USER_ID, productId)
                // BUG FIX: was throwing CartItemNotFoundException here, which
                // is the wrong exception for the wrong resource - it produces
                // a misleading "Cart item not found" message when the user
                // was actually deleting a wishlist item. Each resource type
                // needs its own not-found exception.
                .orElseThrow(() -> new WishlistItemNotFoundException(productId));
    }

    private WishlistResponseDto toDto(Wishlist wishlist) {
        Product product = wishlist.getProduct();

        return new WishlistResponseDto(
                wishlist.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl()
        );
    }
}