package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.entities.CompanyPaymentMethods;

public interface CompanyPaymentMethodsRepository extends JpaRepository<CompanyPaymentMethods, String> {
    public List<CompanyPaymentMethods> findByCompanyId(String companyId);

    @Modifying
    @Transactional
    @Query("""
        delete from company_payment_methods c
        where c.company.id = :companyId
    """)
    void deleteByCompanyId(@Param("companyId") String companyId);
}
