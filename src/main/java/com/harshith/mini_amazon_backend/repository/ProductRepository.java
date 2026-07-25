package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name,
            String brand,
            String category
    );

    List<Product> findByCategoryIgnoreCase(String category);
}