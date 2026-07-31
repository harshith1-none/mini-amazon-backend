package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.WishlistResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.entity.Wishlist;
import com.harshith.mini_amazon_backend.exception.DuplicateWishlistItemException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.exception.WishlistItemNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.repository.WishlistRepository;
import com.harshith.mini_amazon_backend.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository,
                           CurrentUserProvider currentUserProvider) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Day 7: was keyed on DEFAULT_USER_ID = 1L - see CartServiceImpl for
    // the same fix and reasoning. Every method now resolves the actual
    // logged-in user first.
    public List<WishlistResponseDto> getWishlist() {
        User currentUser = currentUserProvider.getCurrentUser();
        return wishlistRepository.findByUser(currentUser)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // BUG FIX (pre-existing): the previous version silently returned the
    // existing item when the product was already wishlisted, instead of
    // rejecting the request. A duplicate POST must fail loudly with 409
    // Conflict, not succeed silently, or the caller has no way to know
    // their request was a no-op.
    public WishlistResponseDto addItem(Long productId) {
        User currentUser = currentUserProvider.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (wishlistRepository.existsByUserAndProductId(currentUser, productId)) {
            throw new DuplicateWishlistItemException(productId);
        }

        Wishlist wishlistItem = new Wishlist();
        wishlistItem.setUser(currentUser);
        wishlistItem.setProduct(product);

        return toDto(wishlistRepository.save(wishlistItem));
    }

    public void deleteItem(Long productId) {
        Wishlist wishlistItem = getWishlistItemOrThrow(productId);
        wishlistRepository.delete(wishlistItem);
    }

    private Wishlist getWishlistItemOrThrow(Long productId) {
        User currentUser = currentUserProvider.getCurrentUser();
        return wishlistRepository.findByUserAndProductId(currentUser, productId)
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