package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ProductRequestDto;
import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
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

}