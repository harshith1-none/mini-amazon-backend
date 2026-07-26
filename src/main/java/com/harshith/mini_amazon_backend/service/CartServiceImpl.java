package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.CartResponseDto;
import com.harshith.mini_amazon_backend.entity.Cart;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.CartItemNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.CartRepository;
import com.harshith.mini_amazon_backend.repository.ProductRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private static final Long DEFAULT_USER_ID = 1L;

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartResponseDto> getCart() {
        return cartRepository.findByUserId(DEFAULT_USER_ID)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CartResponseDto addItem(Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Cart cartItem = cartRepository.findByUserIdAndProductId(DEFAULT_USER_ID, productId)
                .orElse(null);

        if (cartItem == null) {
            cartItem = new Cart();
            cartItem.setUserId(DEFAULT_USER_ID);
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

        //    @Override
        //    public void clearCart() {
        //        cartRepository.deleteByUserId(DEFAULT_USER_ID);
        //    }
        // day 4 backend - BUG FIX
        // cartRepository.deleteByUserId(...) is a derived delete query, not a
        // built-in CRUD method - unlike cartRepository.delete(entity) (used in
        // removeItem above), which Spring Data already wraps in a transaction
        // internally, a custom deleteByX query needs its own explicit
        // transaction boundary or it fails at runtime trying to access the
        // persistence context. That's why only Clear Cart was throwing a 500 -
        // Remove Item never hit this problem because it uses a different,
        // already-transactional method.
        @Override
        @Transactional
        public void clearCart() {
            cartRepository.deleteByUserId(DEFAULT_USER_ID);
        }


    private Cart getCartItemOrThrow(Long productId) {
        return cartRepository.findByUserIdAndProductId(DEFAULT_USER_ID, productId)
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