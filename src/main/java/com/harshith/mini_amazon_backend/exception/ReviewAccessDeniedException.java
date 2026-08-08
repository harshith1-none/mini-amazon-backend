package com.harshith.mini_amazon_backend.exception;

// Distinct from Spring Security's AccessDeniedException (handled by
// CustomAccessDeniedHandler for role-based rejections, e.g. a non-admin
// hitting an admin-only endpoint). This one is a business-rule 403: the
// user IS authenticated and IS allowed to hit PUT/DELETE /api/reviews/{id}
// in general, they just don't own this specific review. Same "authenticated
// but not allowed" outcome, different reason, so it's modelled as its own
// exception and handled in GlobalExceptionHandler like every other
// domain-specific error in this project.
public class ReviewAccessDeniedException extends RuntimeException {
    public ReviewAccessDeniedException(Long reviewId) {
        super("You do not have permission to modify review id " + reviewId);
    }
}