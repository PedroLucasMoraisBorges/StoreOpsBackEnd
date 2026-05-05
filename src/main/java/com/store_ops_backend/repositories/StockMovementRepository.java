package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, String> {
    List<StockMovement> findByStockItemIdOrderByCreatedAtDesc(String stockItemId);
    List<StockMovement> findByStockItemIdInOrderByCreatedAtDesc(List<String> stockItemIds);
    List<StockMovement> findByCompanyIdOrderByCreatedAtDesc(String companyId);
}
