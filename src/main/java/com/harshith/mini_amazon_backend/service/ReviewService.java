package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ReviewRequestDto;
import com.harshith.mini_amazon_backend.dto.ReviewResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.Review;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.exception.DuplicateReviewException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.exception.ReviewAccessDeniedException;
import com.harshith.mini_amazon_backend.exception.ReviewNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.repository.ReviewRepository;
import com.harshith.mini_amazon_backend.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         CurrentUserProvider currentUserProvider) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Business rules enforced here (per today's spec):
    //   - user must be logged in            -> currentUserProvider.getCurrentUser()
    //     (also enforced at the URL level in SecurityConfig, but the
    //     service still needs the actual User to stamp onto the review)
    //   - product must exist                -> ProductNotFoundException
    //   - one user cannot review a product
    //     twice                             -> DuplicateReviewException
    public ReviewResponseDto addReview(Long productId, ReviewRequestDto request) {
        User currentUser = currentUserProvider.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (reviewRepository.existsByUserAndProductId(currentUser, productId)) {
            throw new DuplicateReviewException(productId);
        }

        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toDto(reviewRepository.save(review));
    }

    // Public endpoint (see SecurityConfig - GET /api/products/** is
    // permitAll), so no currentUserProvider call here at all. Still checks
    // the product exists first: returning an empty list for a product id
    // that was never real would silently hide a client-side bug (typo'd
    // id, stale link) behind what looks like "just no reviews yet".
    public List<ReviewResponseDto> getProductReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto request) {
        Review review = getOwnedReviewOrThrow(reviewId);

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toDto(reviewRepository.save(review));
    }

    public void deleteReview(Long reviewId) {
        Review review = getOwnedReviewOrThrow(reviewId);
        reviewRepository.delete(review);
    }

    // Shared by updateReview/deleteReview: fetch-then-check-ownership.
    // Note this is intentionally NOT the same IDOR-safe pattern
    // OrderRepository.findByIdAndUser uses (which makes "doesn't exist"
    // and "exists but isn't yours" indistinguishable to the caller, on
    // purpose, for orders). Reviews are already visible to everyone via
    // GET /api/products/{id}/reviews, so there's no id-existence secret to
    // protect here - a review's existence is public information, only its
    // *modification* is restricted. That's why this deliberately reports
    // 404 (doesn't exist) vs 403 (exists, not yours) as two different,
    // more informative outcomes instead of collapsing them into one.
    private Review getOwnedReviewOrThrow(Long reviewId) {
        User currentUser = currentUserProvider.getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ReviewAccessDeniedException(reviewId);
        }

        return review;
    }

    private ReviewResponseDto toDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}