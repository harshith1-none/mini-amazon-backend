package com.harshith.mini_amazon_backend.dto;

import com.harshith.mini_amazon_backend.entity.Product;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistRequestDto {

    @NotNull(message = "Product Id should not be empty")
    private Long productId;
}
