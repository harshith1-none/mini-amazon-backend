package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.CartResponseDto;
import com.harshith.mini_amazon_backend.entity.Cart;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.exception.CartItemNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.CartRepository;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.security.CurrentUserProvider;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public CartServiceImpl(CartRepository cartRepository,
                           ProductRepository productRepository,
                           CurrentUserProvider currentUserProvider) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Day 7: every method below was previously keyed on a hard-coded
    // DEFAULT_USER_ID = 1L, meaning every request from every user read and
    // wrote the exact same cart rows. That constant is gone - each method
    // now resolves the actual logged-in user via CurrentUserProvider first,
    // which is what makes each user's cart their own.
    @Override
    public List<CartResponseDto> getCart() {
        User currentUser = currentUserProvider.getCurrentUser();
        return cartRepository.findByUser(currentUser)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CartResponseDto addItem(Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        User currentUser = currentUserProvider.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Cart cartItem = cartRepository.findByUserAndProductId(currentUser, productId)
                .orElse(null);

        if (cartItem == null) {
            cartItem = new Cart();
            cartItem.setUser(currentUser);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        return toDto(cartRepository.save(cartItem));
    }

    // day 4 backend (REST cleanup)
    // Replaces the old increaseQuantity/decreaseQuantity methods. PUT sets
    // the item's quantity to an explicit value rather than nudging it by
    // 1 - properly idempotent, and it's the frontend's job now to compute
    // current +/- 1 and send the result. The floor of 1 is still enforced
    // here regardless of what the client sends.
    @Override
    public CartResponseDto updateQuantity(Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Cart cartItem = getCartItemOrThrow(productId);
        cartItem.setQuantity(quantity);
        return toDto(cartRepository.save(cartItem));
    }

    @Override
    public void removeItem(Long productId) {
        Cart cartItem = getCartItemOrThrow(productId);
        cartRepository.delete(cartItem);
    }

    // day 4 backend - BUG FIX
    // cartRepository.deleteByUser(...) is a derived delete query, not a
    // built-in CRUD method - unlike cartRepository.delete(entity) (used in
    // removeItem above), which Spring Data already wraps in a transaction
    // internally, a custom deleteByX query needs its own explicit
    // transaction boundary or it fails at runtime trying to access the
    // persistence context.
    @Override
    @Transactional
    public void clearCart() {
        cartRepository.deleteByUser(currentUserProvider.getCurrentUser());
    }

    private Cart getCartItemOrThrow(Long productId) {
        User currentUser = currentUserProvider.getCurrentUser();
        return cartRepository.findByUserAndProductId(currentUser, productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
    }

    private CartResponseDto toDto(Cart cart) {
        Product product = cart.getProduct();
        BigDecimal lineTotal = product.getCost().multiply(BigDecimal.valueOf(cart.getQuantity()));

        return new CartResponseDto(
                cart.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getCost(),
                cart.getQuantity(),
                lineTotal
        );
    }
}