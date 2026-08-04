package com.harshith.mini_amazon_backend.dto;

import com.harshith.mini_amazon_backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Bound directly to the OrderStatus enum. Jackson rejects any value that
// isn't one of PLACED/PROCESSING/SHIPPED/DELIVERED/CANCELLED with a 400
// before this DTO is even built, so garbage strings never reach the
// service layer - only genuinely valid-but-wrong transitions (checked in
// OrderServiceImpl) need their own handling.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdateRequestDto {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}