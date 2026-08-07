package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.WishlistResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.entity.Role;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.entity.Wishlist;
import com.harshith.mini_amazon_backend.exception.DuplicateWishlistItemException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.exception.WishlistItemNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.repository.WishlistRepository;
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

// Same pattern as CartServiceTest: WishlistService's collaborators
// (WishlistRepository, ProductRepository, CurrentUserProvider) are mocked
// with Mockito so these tests exercise only the service's own logic -
// no Spring context, no database.
@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private WishlistService wishlistService;

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
        product.setImageUrl("mouse.jpg");
    }

    // ---------- getWishlist ----------

    @Test
    void getWishlist_returnsCurrentUsersItemsMappedToDto() {
        Wishlist wishlistItem = new Wishlist();
        wishlistItem.setId(100L);
        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUser(user)).thenReturn(List.of(wishlistItem));

        List<WishlistResponseDto> result = wishlistService.getWishlist();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(0).getProductId()).isEqualTo(10L);
        assertThat(result.get(0).getProductName()).isEqualTo("Wireless Mouse");
        assertThat(result.get(0).getProductImageUrl()).isEqualTo("mouse.jpg");
    }

    @Test
    void getWishlist_noItems_returnsEmptyList() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUser(user)).thenReturn(List.of());

        List<WishlistResponseDto> result = wishlistService.getWishlist();

        assertThat(result).isEmpty();
    }

    // ---------- addItem ----------

    @Test
    void addItem_newProduct_savesAndReturnsDto() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProductId(user, 10L)).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        WishlistResponseDto result = wishlistService.addItem(10L);

        ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);
        verify(wishlistRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getProduct()).isEqualTo(product);
        assertThat(result.getProductId()).isEqualTo(10L);
        assertThat(result.getProductName()).isEqualTo("Wireless Mouse");
    }

    @Test
    void addItem_productNotFound_throwsProductNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.addItem(99L))
                .isInstanceOf(ProductNotFoundException.class);

        // Never even checks for a duplicate, let alone saves, once the
        // product itself doesn't exist.
        verify(wishlistRepository, never()).existsByUserAndProductId(any(), any());
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void addItem_duplicateProduct_throwsDuplicateWishlistItemException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProductId(user, 10L)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addItem(10L))
                .isInstanceOf(DuplicateWishlistItemException.class)
                .hasMessageContaining("10");

        // A duplicate must fail loudly, not silently save a second row or
        // return the existing one.
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    // ---------- deleteItem ----------

    @Test
    void deleteItem_found_deletesWishlistItem() {
        Wishlist existing = new Wishlist();
        existing.setId(100L);
        existing.setUser(user);
        existing.setProduct(product);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.of(existing));

        wishlistService.deleteItem(10L);

        verify(wishlistRepository).delete(existing);
    }

    @Test
    void deleteItem_notFound_throwsWishlistItemNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserAndProductId(user, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.deleteItem(10L))
                .isInstanceOf(WishlistItemNotFoundException.class);

        verify(wishlistRepository, never()).delete(any(Wishlist.class));
    }
}