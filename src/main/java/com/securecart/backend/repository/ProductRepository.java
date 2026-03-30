package com.securecart.backend.repository;

import com.securecart.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByEnabledTrueOrderByCreatedAtDesc();

    Optional<Product> findByIdAndEnabledTrue(Long id);

    List<Product> findByNameContainingIgnoreCaseAndEnabledTrue(String name);

    List<Product> findByCategoryIdAndEnabledTrue(Long categoryId);
}
