package com.harshith.mini_amazon_backend.controller;


import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


//    //day 2 backend
//    @PostMapping()
//    public Product addProduct(Product product) {
//        return productService.addProduct(product);
//    }
//
//    @PutMapping("/{id}")
//    public Product updateProduct(@PathVariable Long id,Product product) {
//        return productService.updateProduct(id,product);
//    }
//
//    @DeleteMapping("/{id}")
//    public void deleteProduct(@PathVariable Long id)  {
//         productService.deleteProduct(id);
//    }
}