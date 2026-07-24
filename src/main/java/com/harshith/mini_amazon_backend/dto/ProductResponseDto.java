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
}