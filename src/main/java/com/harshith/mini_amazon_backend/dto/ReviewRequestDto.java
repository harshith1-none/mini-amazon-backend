package com.harshith.mini_amazon_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for both POST /api/products/{productId}/reviews (create)
 * and PUT /api/reviews/{reviewId} (update) - same pattern as
 * CartRequestDto being reused for both add and update quantity. A review
 * only ever has two client-editable fields, so a separate
 * ReviewUpdateRequestDto would just duplicate these same two validated
 * fields for no benefit.
 *
 * No "rating" default like CartRequestDto.quantity = 1 - there's no
 * sensible default star rating, so leaving it unset (0) deliberately
 * fails the @Min(1) check below rather than silently submitting a
 * 1-star review nobody chose.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDto {

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private int rating;

    @NotBlank(message = "Comment must not be blank")
    private String comment;
}