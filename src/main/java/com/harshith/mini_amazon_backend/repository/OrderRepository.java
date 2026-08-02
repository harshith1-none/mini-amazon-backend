package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Order;
import com.harshith.mini_amazon_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // UPDATED (Day 9 review): plain findByUserOrderByOrderDateDesc would
    // lazy-load orderItems (1 query per order) and each item's product
    // (1 query per item) as toDto() loops over them - classic N+1. JOIN
    // FETCH pulls order + orderItems + product in a single SQL query.
    // DISTINCT is required here: joining Order (1) to OrderItem (many)
    // duplicates the parent Order row once per item row at the SQL level,
    // and DISTINCT at the JPQL level collapses those back down to one
    // Order per id before Hibernate hands the list back.
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.user = :user " +
            "ORDER BY o.orderDate DESC")
    List<Order> findByUserWithItemsOrderByOrderDateDesc(@Param("user") User user);

    // UPDATED (Day 9 review): same N+1 reasoning as above, applied to the
    // single-order lookup. Still combines id + user in one query - "does
    // this order exist AND belong to this user" stays a single check, so
    // the IDOR protection from the original version is unchanged.
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.id = :id AND o.user = :user")
    Optional<Order> findByIdAndUserWithItems(@Param("id") Long id, @Param("user") User user);
}