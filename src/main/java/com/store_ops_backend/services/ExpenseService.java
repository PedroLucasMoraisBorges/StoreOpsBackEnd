package com.store_ops_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CreateExpenseDTO;
import com.store_ops_backend.models.dtos.ExpenseResponseDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.CompanyExpense;
import com.store_ops_backend.repositories.CompanyExpenseRepository;
import com.store_ops_backend.repositories.CompanyRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ExpenseService {

    @Autowired
    private CompanyExpenseRepository expenseRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public ExpenseResponseDTO create(String companyId, CreateExpenseDTO data) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));

        CompanyExpense expense = new CompanyExpense(
            company, data.amount(), data.description(), data.category(), data.expenseDate()
        );
        return toResponse(expenseRepository.save(expense));
    }

    public List<ExpenseResponseDTO> listAll(String companyId) {
        return expenseRepository.findByCompanyIdOrderByExpenseDateDesc(companyId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(String companyId, String expenseId) {
        CompanyExpense expense = expenseRepository.findByIdAndCompanyId(expenseId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada"));
        expenseRepository.delete(expense);
    }

    private ExpenseResponseDTO toResponse(CompanyExpense e) {
        return new ExpenseResponseDTO(
            e.getId(), e.getAmount(), e.getDescription(),
            e.getCategory(), e.getExpenseDate(), e.getCreatedAt()
        );
    }
}
