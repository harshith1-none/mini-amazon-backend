package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ProductRequestDto;
import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.CategoryNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Wireless Mouse");
        product.setBrand("Logitech");
        product.setCategory("Electronics");
        product.setDescription("Ergonomic wireless mouse");
        product.setCost(new BigDecimal("799.00"));
        product.setRating(4.5);
        product.setStock(50);
        product.setImageUrl("mouse.jpg");
        product.setNewArrival(true);
        product.setOnSale(false);
        product.setDiscountPercent(0);
    }

    // ---------- getAllProducts ----------

    @Test
    void getAllProducts_noSort_returnsAllProductsMappedToDto() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.getAllProducts(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Wireless Mouse");
        assertThat(result.get(0).getCost()).isEqualByComparingTo("799.00");
        verify(productRepository).findAll();
        verify(productRepository, never()).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void getAllProducts_withValidSort_appliesSortAndReturnsProducts() {
        when(productRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.getAllProducts("cost,asc");

        assertThat(result).hasSize(1);
        verify(productRepository).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void getAllProducts_invalidSortFormat_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> productService.getAllProducts("cost"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort format");

        verify(productRepository, never()).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void getAllProducts_unsortableField_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> productService.getAllProducts("description,asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot sort by");
    }

    @Test
    void getAllProducts_invalidDirection_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> productService.getAllProducts("cost,sideways"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort direction");
    }

    // ---------- getProductById ----------

    @Test
    void getProductById_found_returnsDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDto result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBrand()).isEqualTo("Logitech");
    }

    @Test
    void getProductById_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- addProduct ----------

    @Test
    void addProduct_savesAndReturnsDto() {
        ProductRequestDto request = new ProductRequestDto(
                "Keyboard", "Logitech", "Electronics", "Mechanical keyboard",
                new BigDecimal("2499.00"), 4.2, 20, "keyboard.jpg", true, false, 0);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ProductResponseDto result = productService.addProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Keyboard");
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getCost()).isEqualByComparingTo("2499.00");
    }

    // ---------- updateProduct ----------

    @Test
    void updateProduct_found_updatesAndReturnsDto() {
        ProductRequestDto request = new ProductRequestDto(
                "Wireless Mouse Pro", "Logitech", "Electronics", "Updated description",
                new BigDecimal("899.00"), 4.7, 40, "mouse-pro.jpg", false, true, 10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDto result = productService.updateProduct(1L, request);

        assertThat(result.getName()).isEqualTo("Wireless Mouse Pro");
        assertThat(result.getCost()).isEqualByComparingTo("899.00");
        assertThat(result.isOnSale()).isTrue();
        assertThat(result.getDiscountPercent()).isEqualTo(10);
    }

    @Test
    void updateProduct_notFound_throwsProductNotFoundException() {
        ProductRequestDto request = new ProductRequestDto(
                "X", "Y", "Z", "desc", BigDecimal.TEN, 4.0, 1, "img.jpg", false, false, 0);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    // ---------- deleteProduct ----------

    @Test
    void deleteProduct_exists_deletesSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throwsProductNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }

    // ---------- searchProducts ----------

    @Test
    void searchProducts_returnsMatchingProducts() {
        when(productRepository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                eq("mouse"), eq("mouse"), eq("mouse")))
                .thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.searchProducts("mouse");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Wireless Mouse");
    }

    // ---------- categoryFilter ----------

    @Test
    void categoryFilter_found_returnsProducts() {
        when(productRepository.findByCategoryIgnoreCase("Electronics")).thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.categoryFilter("Electronics");

        assertThat(result).hasSize(1);
    }

    @Test
    void categoryFilter_empty_throwsCategoryNotFoundException() {
        when(productRepository.findByCategoryIgnoreCase("Furniture")).thenReturn(List.of());

        assertThatThrownBy(() -> productService.categoryFilter("Furniture"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Furniture");
    }
}
