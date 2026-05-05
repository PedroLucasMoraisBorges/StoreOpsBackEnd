package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductIdAndActiveTrue(String productId);
}
