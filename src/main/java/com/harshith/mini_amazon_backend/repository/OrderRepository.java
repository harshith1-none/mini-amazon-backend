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


    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.user = :user " +
            "ORDER BY o.orderDate DESC")
    List<Order> findByUserWithItemsOrderByOrderDateDesc(@Param("user") User user);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.id = :id AND o.user = :user")
    Optional<Order> findByIdAndUserWithItems(@Param("id") Long id, @Param("user") User user);


    // NEW (Day 10): admin status updates operate on ANY user's order, not
    // just the caller's own - deliberately no "AND o.user = :user" filter
    // here, unlike findByIdAndUserWithItems above. Same JOIN FETCH reasoning:
    // the response DTO needs orderItems + product, so fetch them in one
    // query instead of lazy-loading each while building the DTO.
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}