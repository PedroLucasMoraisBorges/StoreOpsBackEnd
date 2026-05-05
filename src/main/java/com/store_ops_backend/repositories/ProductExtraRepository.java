package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.ProductExtra;

public interface ProductExtraRepository extends JpaRepository<ProductExtra, String> {
    List<ProductExtra> findByProductIdAndActiveTrue(String productId);
}
