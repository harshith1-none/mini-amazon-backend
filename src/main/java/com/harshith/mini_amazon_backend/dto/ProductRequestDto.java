package com.harshith.mini_amazon_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload for creating/updating a product.
 *
 * This is intentionally separate from the Product entity and has NO "id"
 * field. If the controller bound requests straight to the entity, a client
 * could put an "id" in the JSON body and potentially overwrite a different
 * product, or Spring would try to bind unknown/entity-only fields. Using a
 * dedicated request DTO also lets create/update validation evolve
 * independently of how the entity is persisted.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost cannot be negative")
    private BigDecimal cost;

    @Min(value = 0, message = "Rating cannot be less than 0")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private double rating;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    private String imageUrl;

    private boolean newArrival;

    private boolean onSale;

    @Min(value = 0, message = "Discount percent cannot be negative")
    @Max(value = 100, message = "Discount percent cannot exceed 100")
    private int discountPercent;
}