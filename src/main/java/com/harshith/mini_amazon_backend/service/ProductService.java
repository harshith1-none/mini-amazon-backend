package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;

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


//    //day 2 backend
//    public Product addProduct(Product product) {
//        return productRepository.save(product);
//    }
//
//    public Product updateProduct(Long id,Product product) {
//        Product p = productRepository.findById(id)
//                .orElseThrow(() -> new ProductNotFoundException(id));
//        return productRepository.save(product);
//    }
//
//
//    public void deleteProduct(Long id) {
//        productRepository.deleteById(id);
//    }
}