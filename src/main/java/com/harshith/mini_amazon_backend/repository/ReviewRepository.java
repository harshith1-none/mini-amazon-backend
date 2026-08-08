package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Review;
import com.harshith.mini_amazon_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByUserAndProductId(User user, Long productId);

    Optional<Review> findByUserAndProductId(User user, Long productId);

    // Used for SINGLE-product lookups (getProductById, addProduct,
    // updateProduct in ProductService) where one extra query per call is
    // fine. AVG() over zero matching rows returns one row with a null
    // value (there's no GROUP BY here, so the aggregate always returns
    // exactly one row) - the service layer treats that null as "no
    // reviews yet" -> 0.0, not as an error.
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    long countByProductId(Long productId);

    // Used for LIST endpoints (getAllProducts, searchProducts,
    // categoryFilter) to avoid an N+1: one aggregate query grouped by
    // product, instead of calling findAverageRatingByProductId/
    // countByProductId once per product in a loop. Products with zero
    // reviews simply won't appear in this result at all (GROUP BY only
    // produces rows for groups that exist) - the service layer treats a
    // missing entry the same way it treats a null average above.
    interface ProductRatingSummary {
        Long getProductId();
        Double getAverageRating();
        Long getReviewCount();
    }

    @Query("SELECT r.product.id AS productId, AVG(r.rating) AS averageRating, COUNT(r) AS reviewCount " +
            "FROM Review r GROUP BY r.product.id")
    List<ProductRatingSummary> findRatingSummaryForAllProducts();
}
