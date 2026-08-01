package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Not called directly by OrderServiceImpl today - order items are always
// created and read through their parent Order (cascade handles persistence).
// Included for consistency with the one-repository-per-entity convention
// used elsewhere in the project, and so it's ready for future per-item
// queries (e.g. "best selling products") without restructuring later.
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}