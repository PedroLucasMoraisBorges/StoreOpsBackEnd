package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.StoreTable;

import jakarta.persistence.LockModeType;

public interface StoreTableRepository extends JpaRepository<StoreTable, String> {
    List<StoreTable> findByCompanyIdOrderByNumberAsc(String companyId);
    Optional<StoreTable> findByIdAndCompanyId(String id, String companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM store_tables t WHERE t.id = :id AND t.company.id = :companyId")
    Optional<StoreTable> findByIdAndCompanyIdForUpdate(@Param("id") String id, @Param("companyId") String companyId);

    boolean existsByCompanyIdAndNumber(String companyId, Integer number);
}
