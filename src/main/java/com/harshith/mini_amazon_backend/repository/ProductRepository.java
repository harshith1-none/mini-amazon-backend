package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;

import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


//day 2
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    //day 3
    List<Product> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name,
                                                                                                          String brand,
                                                                                                          String category);

    List<Product> findByCategoryIgnoreCase(String category);
}
