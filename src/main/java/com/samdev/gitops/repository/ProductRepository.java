package com.samdev.gitops.repository;

import com.samdev.gitops.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by name containing (case-insensitive search)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find products within a price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find products still in stock
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findInStock();
}