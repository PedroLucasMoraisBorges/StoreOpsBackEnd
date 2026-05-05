package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.StoreTable;

public interface StoreTableRepository extends JpaRepository<StoreTable, String> {
    List<StoreTable> findByCompanyIdOrderByNumberAsc(String companyId);
    Optional<StoreTable> findByIdAndCompanyId(String id, String companyId);
    boolean existsByCompanyIdAndNumber(String companyId, Integer number);
}
