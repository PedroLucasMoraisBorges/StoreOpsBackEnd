package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.ProductComponentGroup;

public interface ProductComponentGroupRepository extends JpaRepository<ProductComponentGroup, String> {
    List<ProductComponentGroup> findByProductIdAndActiveTrue(String productId);
    Optional<ProductComponentGroup> findByIdAndProductId(String id, String productId);
}
