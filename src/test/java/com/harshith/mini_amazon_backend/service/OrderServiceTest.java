package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.OrderResponseDto;
import com.harshith.mini_amazon_backend.entity.Cart;
import com.harshith.mini_amazon_backend.entity.Order;
import com.harshith.mini_amazon_backend.entity.OrderItem;
import com.harshith.mini_amazon_backend.entity.OrderStatus;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.Role;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.exception.EmptyCartException;
import com.harshith.mini_amazon_backend.exception.InsufficientStockException;
import com.harshith.mini_amazon_backend.exception.InvalidOrderStatusTransitionException;
import com.harshith.mini_amazon_backend.exception.OrderNotFoundException;
import com.harshith.mini_amazon_backend.repository.CartRepository;
import com.harshith.mini_amazon_backend.repository.OrderRepository;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.security.CurrentUserProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests OrderServiceImpl directly (same reasoning as CartServiceTest -
// @InjectMocks needs a concrete class).
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Harshu");
        user.setEmail("harshu@example.com");
        user.setPassword("hashed");
        user.setRole(Role.USER);

        product = new Product();
        product.setId(10L);
        product.setName("Wireless Mouse");
        product.setBrand("Logitech");
        product.setCategory("Electronics");
        product.setCost(new BigDecimal("500.00"));
        product.setStock(10);
    }

    // ---------- placeOrder ----------

    @Test
    void placeOrder_emptyCart_throwsEmptyCartException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder())
                .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void placeOrder_insufficientStock_throwsInsufficientStockExceptionAndSavesNothing() {
        Cart cartItem = new Cart();
        cartItem.setId(200L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(15); // more than the 10 in stock

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.placeOrder())
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Wireless Mouse");

        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
        verify(cartRepository, never()).deleteByUser(any(User.class));
    }

    @Test
    void placeOrder_success_reservesStockCreatesOrderAndClearsCart() {
        Cart cartItem = new Cart();
        cartItem.setId(200L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(500L);
            return savedOrder;
        });

        OrderResponseDto result = orderService.placeOrder();

        // Stock reserved: 10 - 3 = 7
        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository).save(product);

        // Order built and saved correctly
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUser()).isEqualTo(user);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(savedOrder.getOrderItems()).hasSize(1);
        assertThat(savedOrder.getOrderItems().get(0).getPriceAtPurchase()).isEqualByComparingTo("500.00");

        // Cart cleared after the order is placed
        verify(cartRepository).deleteByUser(user);

        // Response DTO reflects what was saved
        assertThat(result.getId()).isEqualTo(500L);
        assertThat(result.getStatus()).isEqualTo("PLACED");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.getItems()).hasSize(1);
    }

    // ---------- getOrders ----------

    @Test
    void getOrders_returnsCurrentUsersOrdersMappedToDto() {
        Order order = buildOrderWithOneItem(1L, OrderStatus.PLACED);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserWithItemsOrderByOrderDateDesc(user)).thenReturn(List.of(order));

        List<OrderResponseDto> result = orderService.getOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getItems()).hasSize(1);
    }

    // ---------- getOrderById ----------

    @Test
    void getOrderById_found_returnsDto() {
        Order order = buildOrderWithOneItem(1L, OrderStatus.PLACED);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserWithItems(1L, user)).thenReturn(Optional.of(order));

        OrderResponseDto result = orderService.getOrderById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_notFound_throwsOrderNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserWithItems(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- updateOrderStatus ----------

    @Test
    void updateOrderStatus_validTransition_updatesAndReturnsDto() {
        Order order = buildOrderWithOneItem(1L, OrderStatus.PLACED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

        assertThat(result.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void updateOrderStatus_sameStatus_throwsInvalidOrderStatusTransitionException() {
        Order order = buildOrderWithOneItem(1L, OrderStatus.PLACED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PLACED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_disallowedTransition_throwsInvalidOrderStatusTransitionException() {
        // PLACED can only go to PROCESSING or CANCELLED, not straight to DELIVERED.
        Order order = buildOrderWithOneItem(1L, OrderStatus.PLACED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_terminalStatus_throwsInvalidOrderStatusTransitionException() {
        // DELIVERED has no entry in VALID_TRANSITIONS at all - any further
        // transition attempt must be rejected, not throw an unrelated error.
        Order order = buildOrderWithOneItem(1L, OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsOrderNotFoundException() {
        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, OrderStatus.PROCESSING))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ---------- helpers ----------

    private Order buildOrderWithOneItem(Long orderId, OrderStatus status) {
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("1500.00"));
        order.setOrderDate(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(900L);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setPriceAtPurchase(new BigDecimal("500.00"));

        order.setOrderItems(List.of(item));
        return order;
    }
}

