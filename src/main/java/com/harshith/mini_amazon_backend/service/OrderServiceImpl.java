package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.OrderItemResponseDto;
import com.harshith.mini_amazon_backend.dto.OrderResponseDto;
import com.harshith.mini_amazon_backend.entity.Cart;
import com.harshith.mini_amazon_backend.entity.Order;
import com.harshith.mini_amazon_backend.entity.OrderItem;
import com.harshith.mini_amazon_backend.entity.OrderStatus;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.exception.EmptyCartException;
import com.harshith.mini_amazon_backend.exception.InsufficientStockException;
import com.harshith.mini_amazon_backend.exception.OrderNotFoundException;
import com.harshith.mini_amazon_backend.repository.CartRepository;
import com.harshith.mini_amazon_backend.repository.OrderRepository;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.security.CurrentUserProvider;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            ProductRepository productRepository,
                            CurrentUserProvider currentUserProvider) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public OrderResponseDto placeOrder() {
        User currentUser = currentUserProvider.getCurrentUser();

        List<Cart> cartItems = cartRepository.findByUser(currentUser);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PLACED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), product.getStock(), cartItem.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getCost());
            orderItems.add(orderItem);

            total = total.add(product.getCost().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // Reserve the stock now that this line is confirmed valid.
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteByUser(currentUser);

        return toDto(savedOrder);
    }

    // UPDATED (Day 9): now calls findByUserWithItemsOrderByOrderDateDesc
    // instead of findByUserOrderByOrderDateDesc. Same result set, but the
    // repository query now JOIN FETCHes orderItems and product in one SQL
    // round trip instead of lazy-loading them one order/item at a time
    // while toDto() loops below (see OrderRepository for the full reasoning).
    @Override
    public List<OrderResponseDto> getOrders() {
        User currentUser = currentUserProvider.getCurrentUser();
        return orderRepository.findByUserWithItemsOrderByOrderDateDesc(currentUser)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // UPDATED (Day 9): same N+1 fix as getOrders(), applied to the
    // single-order lookup via findByIdAndUserWithItems.
    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Order order = orderRepository.findByIdAndUserWithItems(orderId, currentUser)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toDto(order);
    }

    private OrderResponseDto toDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getOrderItems()
                .stream()
                .map(this::toItemDto)
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getTotalAmount(),
                itemDtos
        );
    }

    private OrderItemResponseDto toItemDto(OrderItem item) {
        Product product = item.getProduct();
        BigDecimal lineTotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponseDto(
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                item.getPriceAtPurchase(),
                item.getQuantity(),
                lineTotal
        );
    }
}