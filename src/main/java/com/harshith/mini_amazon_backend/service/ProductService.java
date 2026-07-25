package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ProductRequestDto;
import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.CategoryNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //day1
//    public List<ProductResponseDto> getAllProducts() {
//        return productRepository.findAll()
//                .stream()
//                .map(this::toDto)
//                .toList();
//    }

    //day 3 (get all products & sorting in 1 meathod for production quality)
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


    //day 2 backend
    public ProductResponseDto addProduct(ProductRequestDto request) {
        Product product = toEntity(request);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto request) {

        // Fetch the existing row and update ITS fields, then save that same
        // managed entity. The previous version fetched this entity, ignored
        // it, and saved the incoming (id-less) object instead - since that
        // object had no id, JPA treated it as a brand-new row (an INSERT)
        // rather than updating the row for `id`, so PUT silently created
        // duplicate products instead of editing the original one.

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        applyUpdates(existing, request);
        Product saved = productRepository.save(existing);
        return toDto(saved);
    }

    public void deleteProduct(Long id) {

        // Verify the product exists first. productRepository.deleteById(id)
        // alone throws EmptyResultDataAccessException for a missing id, and
        // since that exception has no handler in GlobalExceptionHandler, it
        // was bubbling up as an unhandled 500 Internal Server Error instead
        // of a clean 404 Not Found.

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private Product toEntity(ProductRequestDto request) {
        Product product = new Product();
        applyUpdates(product,request);
        return product;
    }

    private void applyUpdates(Product product,ProductRequestDto request) {
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


    //day 2 backend
//    public List<ProductResponseDto> searchProducts(@Valid String keyword) {
//        List<Product> products = productRepository
//                .findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
//                        keyword,
//                        keyword,
//                        keyword
//                );
//        return products
//                .stream()
//                .map(this::toDto)
//                .toList();
//    }
//
//    public List<ProductResponseDto> categoryFilter(String category) {
//        List<Product> products = productRepository
//                .findByCategoryIgnoreCase(category);
//        return products .stream() .map(this :: toDto)
//                .toList();
//    }
//
//    public List<ProductResponseDto> sort(String words) {
//
//        String[] parts = words.split(",");
//        String field = parts[0];
//        String direction = parts[1];
//
//        Sort sort = Sort.by(
//                Sort.Direction.fromString(direction),
//                field
//        );
//
//        List<Product> products = productRepository.findAll(sort);
//
//        return products.stream()
//                .map(this::toDto)
//                .toList();
//    }
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


    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "name", "cost", "rating", "stock", "discountPercent"
    );
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
