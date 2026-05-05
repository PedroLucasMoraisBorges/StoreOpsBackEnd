package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCompanyIdOrderByNameAsc(String companyId);
    List<Product> findByCompanyIdAndActiveOrderByNameAsc(String companyId, Boolean active);
    Optional<Product> findByIdAndCompanyId(String id, String companyId);
}
