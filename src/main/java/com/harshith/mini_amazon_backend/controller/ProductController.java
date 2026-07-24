package com.harshith.mini_amazon_backend.controller;


import com.harshith.mini_amazon_backend.dto.ProductRequestDto;
import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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


    @GetMapping()
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    //day 2 backend
    @PostMapping()
    public ResponseEntity<ProductResponseDto> addProduct(@Valid @RequestBody ProductRequestDto request) {

        // @RequestBody was missing before, so Spring never parsed the JSON
        // body into the parameter at all - every field came through as
        // null/0/false regardless of what was sent in Postman. @Valid
        // triggers the bean-validation annotations on ProductRequestDto,
        // which is what actually enforces "Add validation" from today's task.

        ProductResponseDto created = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto updated = productService.updateProduct(id,request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id)  {
         productService.deleteProduct(id);

        // 204 No Content is the correct REST response for a successful
        // delete with no body, instead of the default 200 with an empty body.

         return ResponseEntity.noContent().build();
    }
}