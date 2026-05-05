package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.CompanyExpense;

public interface CompanyExpenseRepository extends JpaRepository<CompanyExpense, String> {
    List<CompanyExpense> findByCompanyIdOrderByExpenseDateDesc(String companyId);
    Optional<CompanyExpense> findByIdAndCompanyId(String id, String companyId);

    @org.springframework.data.jpa.repository.Query("""
        select e from company_expenses e
        where e.company.id = :companyId
        and e.expenseDate between :from and :to
    """)
    List<CompanyExpense> findByCompanyIdAndExpenseDateBetween(
        @org.springframework.data.repository.query.Param("companyId") String companyId,
        @org.springframework.data.repository.query.Param("from") java.time.OffsetDateTime from,
        @org.springframework.data.repository.query.Param("to") java.time.OffsetDateTime to
    );
}
