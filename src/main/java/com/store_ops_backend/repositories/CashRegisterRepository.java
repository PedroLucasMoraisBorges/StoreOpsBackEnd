package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.store_ops_backend.models.entities.CashRegister;

public interface CashRegisterRepository extends JpaRepository<CashRegister, String> {

    @Query("SELECT cr FROM CashRegister cr WHERE cr.company.id = :companyId ORDER BY cr.openedAt DESC")
    List<CashRegister> findByCompanyIdOrderByOpenedAtDesc(@Param("companyId") String companyId);

    @Query("SELECT cr FROM CashRegister cr WHERE cr.company.id = :companyId AND cr.status = 'OPEN'")
    Optional<CashRegister> findOpenByCompanyId(@Param("companyId") String companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cr FROM CashRegister cr WHERE cr.company.id = :companyId AND cr.status = 'OPEN'")
    Optional<CashRegister> findOpenByCompanyIdForUpdate(@Param("companyId") String companyId);

    @Query("SELECT cr FROM CashRegister cr WHERE cr.id = :id AND cr.company.id = :companyId")
    Optional<CashRegister> findByIdAndCompanyId(@Param("id") String id, @Param("companyId") String companyId);
}
