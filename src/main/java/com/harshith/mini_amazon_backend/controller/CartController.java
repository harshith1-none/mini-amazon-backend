package com.harshith.mini_amazon_backend.controller;

import com.harshith.mini_amazon_backend.dto.CartRequestDto;
import com.harshith.mini_amazon_backend.dto.CartResponseDto;
import com.harshith.mini_amazon_backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartResponseDto>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    // day 4 backend (REST cleanup)
    // Replaces POST /api/cart/add/{productId}. The item to add is
    // described entirely by the request body (CartRequestDto), so the URL
    // names a resource (the cart collection) rather than an action ("add").
    @PostMapping
    public ResponseEntity<CartResponseDto> addItem(@Valid @RequestBody CartRequestDto request) {
        CartResponseDto added = cartService.addItem(request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    // day 4 backend (REST cleanup)
    // Replaces PUT /api/cart/increase/{productId} and
    // PUT /api/cart/decrease/{productId}. "increase"/"decrease" are RPC
    // verbs that don't map to an HTTP method. PUT on a specific cart item
    // (identified by productId in the path) that SETS its quantity to an
    // explicit value is properly idempotent and RESTful - the frontend
    // computes the new quantity (current +/- 1) and PUTs the result.
    @PutMapping("/{productId}")
    public ResponseEntity<CartResponseDto> updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody CartRequestDto request) {

        if (!productId.equals(request.getProductId())) {
            throw new IllegalArgumentException(
                    "Path productId (" + productId + ") does not match request body productId ("
                            + request.getProductId() + ")");
        }

        CartResponseDto updated = cartService.updateQuantity(productId, request.getQuantity());
        return ResponseEntity.ok(updated);
    }

    // Replaces DELETE /api/cart/remove/{productId} - productId in the path
    // already uniquely identifies the item, no /remove verb needed.
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId) {
        cartService.removeItem(productId);
        return ResponseEntity.noContent().build();
    }

    // Replaces DELETE /api/cart/clear - DELETE on the collection itself
    // means "delete everything in it", no /clear verb needed.
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}