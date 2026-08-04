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
import com.harshith.mini_amazon_backend.exception.InvalidOrderStatusTransitionException;
import com.harshith.mini_amazon_backend.exception.OrderNotFoundException;
import com.harshith.mini_amazon_backend.repository.CartRepository;
import com.harshith.mini_amazon_backend.repository.OrderRepository;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.security.CurrentUserProvider;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    // NEW (Day 10): the order lifecycle as an explicit graph, not scattered
    // if/else checks. DELIVERED and CANCELLED are terminal - they're simply
    // absent as keys, so any transition attempted from them falls through to
    // "not a valid transition" below. Static so it's built once per class,
    // not once per instance.
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.PLACED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
    }

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

    @Override
    public List<OrderResponseDto> getOrders() {
        User currentUser = currentUserProvider.getCurrentUser();
        return orderRepository.findByUserWithItemsOrderByOrderDateDesc(currentUser)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // NEW (Day 10): admin-only endpoint backs this. Deliberately looks the
    // order up with findByIdWithItems (no user filter) instead of
    // findByIdAndUserWithItems - an admin must be able to update any
    // customer's order, not just their own.
    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            // No-op update (e.g. re-submitting the same status) is treated
            // the same as any other disallowed transition - PLACED does not
            // transition to PLACED, so this correctly falls out of
            // VALID_TRANSITIONS without a separate special case.
            throw new InvalidOrderStatusTransitionException(currentStatus, newStatus);
        }

        Set<OrderStatus> allowedNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        if (allowedNextStatuses == null || !allowedNextStatuses.contains(newStatus)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, newStatus);
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        return toDto(savedOrder);
    }




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