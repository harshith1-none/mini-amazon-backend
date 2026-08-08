package com.harshith.mini_amazon_backend.controller;

import com.harshith.mini_amazon_backend.dto.ReviewRequestDto;
import com.harshith.mini_amazon_backend.dto.ReviewResponseDto;
import com.harshith.mini_amazon_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// No class-level @RequestMapping, unlike every other controller here.
// Every other controller's endpoints all share one resource prefix
// (/api/products, /api/cart, /api/wishlist, /api/orders). Reviews don't:
// create/list are nested under a product (/api/products/{productId}/reviews)
// while update/delete are their own top-level resource (/api/reviews/{id}),
// per today's spec. Forcing a shared class-level prefix would mean fighting
// the required URLs instead of matching them, so each method declares its
// full path instead.
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequestDto request) {
        ReviewResponseDto created = reviewService.addReview(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDto request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request));
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
