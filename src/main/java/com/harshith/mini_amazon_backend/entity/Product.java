package com.harshith.mini_amazon_backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Brand is required")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Min(value = 0, message = "Rating cannot be less than 0")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private double rating;

    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private int stock;

    @Column(name = "image_url")
    private String imageUrl;

    // NOTE: deliberately not named "isNewArrival" to avoid Lombok/Jackson's
    // "is" prefix stripping quirk, where a boolean field starting with "is"
    // generates a getter that Jackson then serializes WITHOUT the "is" prefix
    // (e.g. "isNew" -> JSON key "new"), silently breaking the frontend contract.
    @Column(name = "is_new")
    @JsonProperty("isNew")
    private boolean newArrival;

    @Column(name = "on_sale")
    private boolean onSale;

    @Column(name = "discount_percent")
    private int discountPercent;
}