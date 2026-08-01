package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Order;
import com.harshith.mini_amazon_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Deliberately NOT findById(orderId) alone. Combining id + user in one
    // query means "does this order exist AND belong to this user" - a
    // single check, not two. Looking up by id alone and then comparing
    // order.getUser() afterward is an easy way to accidentally skip the
    // ownership check and let User A read User B's order by guessing an id
    // (an IDOR vulnerability).
    Optional<Order> findByIdAndUser(Long id, User user);
}