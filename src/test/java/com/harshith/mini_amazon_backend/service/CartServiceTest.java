package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.CartResponseDto;
import com.harshith.mini_amazon_backend.entity.Cart;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.Role;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.exception.CartItemNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.CartRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests CartServiceImpl directly, not the CartService interface - Mockito
// needs a concrete class to instantiate with @InjectMocks, and the
// interface has no behaviour of its own to verify.
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CartServiceImpl cartService;

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
        product.setStock(20);
    }

    // ---------- getCart ----------

    @Test
    void getCart_returnsCurrentUsersItemsMappedToDto() {
        Cart cartItem = new Cart();
        cartItem.setId(100L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));

        List<CartResponseDto> result = cartService.getCart();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(10L);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
        assertThat(result.get(0).getLineTotal()).isEqualByComparingTo("1000.00");
    }

    // ---------- addItem ----------

    @Test
    void addItem_newProduct_createsCartItemWithGivenQuantity() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDto result = cartService.addItem(10L, 3);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getProduct()).isEqualTo(product);
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
        assertThat(result.getQuantity()).isEqualTo(3);
    }

    @Test
    void addItem_existingProduct_incrementsExistingQuantity() {
        Cart existing = new Cart();
        existing.setId(100L);
        existing.setUser(user);
        existing.setProduct(product);
        existing.setQuantity(2);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDto result = cartService.addItem(10L, 3);

        assertThat(result.getQuantity()).isEqualTo(5);
        verify(cartRepository).save(existing);
    }

    @Test
    void addItem_quantityLessThanOne_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cartService.addItem(10L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");

        // Validation fails before the current user or product is ever
        // looked up - nothing downstream should be touched.
        verify(currentUserProvider, never()).getCurrentUser();
        verify(productRepository, never()).findById(any());
    }

    @Test
    void addItem_productNotFound_throwsProductNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(99L, 1))
                .isInstanceOf(ProductNotFoundException.class);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    // ---------- updateQuantity ----------

    @Test
    void updateQuantity_validQuantity_updatesAndReturnsDto() {
        Cart existing = new Cart();
        existing.setId(100L);
        existing.setUser(user);
        existing.setProduct(product);
        existing.setQuantity(2);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDto result = cartService.updateQuantity(10L, 7);

        assertThat(result.getQuantity()).isEqualTo(7);
    }

    @Test
    void updateQuantity_lessThanOne_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cartService.updateQuantity(10L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartRepository, never()).findByUserAndProductId(any(), any());
    }

    @Test
    void updateQuantity_itemNotFound_throwsCartItemNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(10L, 5))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    // ---------- removeItem ----------

    @Test
    void removeItem_found_deletesCartItem() {
        Cart existing = new Cart();
        existing.setId(100L);
        existing.setUser(user);
        existing.setProduct(product);
        existing.setQuantity(1);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.of(existing));

        cartService.removeItem(10L);

        verify(cartRepository).delete(existing);
    }

    @Test
    void removeItem_notFound_throwsCartItemNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(10L))
                .isInstanceOf(CartItemNotFoundException.class);

        verify(cartRepository, never()).delete(any(Cart.class));
    }

    // ---------- clearCart ----------

    @Test
    void clearCart_deletesAllItemsForCurrentUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        cartService.clearCart();

        verify(cartRepository).deleteByUser(user);
    }
}