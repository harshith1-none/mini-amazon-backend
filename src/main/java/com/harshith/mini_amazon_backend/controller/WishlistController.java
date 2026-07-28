package com.harshith.mini_amazon_backend.controller;

import com.harshith.mini_amazon_backend.dto.WishlistRequestDto;
import com.harshith.mini_amazon_backend.dto.WishlistResponseDto;
import com.harshith.mini_amazon_backend.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    // BUG FIX: the constructor previously also injected ProductService, but
    // no method in this controller ever used it - a dead, unused dependency
    // that only adds noise and a misleading extra coupling. Removed.
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // BUG FIX: handler methods were declared `private`. Spring MVC invokes
    // controller handler methods reflectively/through a proxy and requires
    // them to be `public` - a private method is either never registered as
    // a route or fails to be invoked correctly depending on the proxying
    // strategy in play. This is a real, not just stylistic, bug.
    @GetMapping
    public ResponseEntity<List<WishlistResponseDto>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist());
    }

    // BUG FIX: was missing @Valid and @RequestBody. Without @RequestBody,
    // Spring does NOT parse the JSON request body into WishlistRequestDto -
    // it instead tries to bind from query/form parameters, so productId
    // would always come through as null and the @NotNull validation on the
    // DTO would never even run (there's nothing to validate the body
    // against). @Valid is what actually triggers that validation.
    @PostMapping
    public ResponseEntity<WishlistResponseDto> addItem(@Valid @RequestBody WishlistRequestDto request) {
        WishlistResponseDto added = wishlistService.addItem(request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    // BUG FIX: previously returned void with no ResponseEntity, which
    // defaults to an HTTP 200 with an empty body. A successful DELETE with
    // no response content should return 204 No Content - this also matches
    // the convention already used by CartController.removeItem.
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long productId) {
        wishlistService.deleteItem(productId);
        return ResponseEntity.noContent().build();
    }
}