package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ProductRequestDto;
import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.CategoryNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "name", "cost", "rating", "stock", "discountPercent"
    );

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> getAllProducts(String sort) {
        List<Product> products = (sort == null || sort.isBlank())
                ? productRepository.findAll()
                : productRepository.findAll(parseSort(sort));

        return products.stream()
                .map(this::toDto)
                .toList();
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return toDto(product);
    }

    private ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getDescription(),
                product.getCost(),
                product.getRating(),
                product.getStock(),
                product.getImageUrl(),
                product.isNewArrival(),
                product.isOnSale(),
                product.getDiscountPercent()
        );
    }

    public ProductResponseDto addProduct(ProductRequestDto request) {
        Product product = toEntity(request);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        applyUpdates(existing, request);
        Product saved = productRepository.save(existing);
        return toDto(saved);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private Product toEntity(ProductRequestDto request) {
        Product product = new Product();
        applyUpdates(product, request);
        return product;
    }

    private void applyUpdates(Product product, ProductRequestDto request) {
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setCost(request.getCost());
        product.setRating(request.getRating());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setNewArrival(request.isNewArrival());
        product.setOnSale(request.isOnSale());
        product.setDiscountPercent(request.getDiscountPercent());
    }

    public List<ProductResponseDto> searchProducts(String keyword) {
        List<Product> products = productRepository
                .findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                );
        return products
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ProductResponseDto> categoryFilter(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category);

        if (products.isEmpty()) {
            throw new CategoryNotFoundException(category);
        }

        return products.stream()
                .map(this::toDto)
                .toList();
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid sort format '" + sort + "'. Expected 'field,direction', e.g. cost,asc");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim();

        if (!SORTABLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    "Cannot sort by '" + field + "'. Allowed fields: " + SORTABLE_FIELDS);
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid sort direction '" + direction + "'. Use 'asc' or 'desc'.");
        }

        return Sort.by(sortDirection, field);
    }
}