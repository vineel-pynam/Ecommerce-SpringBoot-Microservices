package com.vineelpynam.ProductService.repository;

import com.vineelpynam.ProductService.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
