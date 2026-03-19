package com.example.inventory_service.repository;

import com.example.inventory_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Query to avoid race condition by calculating the quantity in java
    @Modifying
    @Query("Update Product p SET p.availableQuantity = p.availableQuantity - :quantity " +
           "WHERE p.id = :productId AND p.availableQuantity >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
