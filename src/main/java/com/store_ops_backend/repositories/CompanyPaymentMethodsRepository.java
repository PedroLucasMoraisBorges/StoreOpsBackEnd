package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.CompanyPaymentMethods;

public interface CompanyPaymentMethodsRepository extends JpaRepository<CompanyPaymentMethods, String> {
    public List<CompanyPaymentMethods> findByCompanyId(String companyId);
}
