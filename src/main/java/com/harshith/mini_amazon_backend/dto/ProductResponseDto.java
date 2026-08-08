package com.harshith.mini_amazon_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private String brand;
    private String category;
    private String description;
    private BigDecimal cost;
    private double rating;
    private int stock;
    private String imageUrl;

    @JsonProperty("isNew")
    private boolean newArrival;

    private boolean onSale;
    private int discountPercent;

    // NEW (Day 14): computed on every read from the Review table (see
    // ProductService.buildDto) - NOT manually maintained here. Storing a
    // running average directly on the product row is exactly the kind of
    // "easily-inconsistent value" today's task warns against: every review
    // add/edit/delete would need to remember to also update this number,
    // and any missed spot (a bug, a direct DB edit, a future bulk-import
    // script) leaves it silently wrong with nothing to catch the drift.
    // Deriving it fresh from Review rows means it's always correct by
    // construction.
    //
    // Note this is a DIFFERENT field from the existing `rating` above.
    // `rating` predates today's task and was always a manually-set,
    // admin-entered baseline number (e.g. from seed data) - it is
    // intentionally left untouched today since removing/renaming it would
    // ripple into ProductRequestDto, the admin product form, and seed data,
    // none of which is in scope for a reviews feature. Worth reconciling
    // the two fields later, flagged in the review notes.
    private double averageRating;
    private long reviewCount;
}