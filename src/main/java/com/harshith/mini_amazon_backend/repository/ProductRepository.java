package com.harshith.mini_amazon_backend.repository;

import com.harshith.mini_amazon_backend.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


}
