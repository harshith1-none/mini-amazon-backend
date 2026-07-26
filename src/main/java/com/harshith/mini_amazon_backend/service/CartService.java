package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.CartResponseDto;

import java.util.List;

public interface CartService {

    List<CartResponseDto> getCart();

    CartResponseDto addItem(Long productId, int quantity);

    CartResponseDto updateQuantity(Long productId, int quantity);

    void removeItem(Long productId);

    void clearCart();
}