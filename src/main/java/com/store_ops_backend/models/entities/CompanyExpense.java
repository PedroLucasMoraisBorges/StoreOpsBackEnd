package com.store_ops_backend.models.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "company_expenses")
@Entity(name = "company_expenses")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CompanyExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private BigDecimal amount;
    private String description;
    private String category;

    @Column(name = "expense_date")
    private OffsetDateTime expenseDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public CompanyExpense(Company company, BigDecimal amount, String description,
                          String category, OffsetDateTime expenseDate) {
        this.company = company;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.expenseDate = expenseDate != null ? expenseDate : OffsetDateTime.now();
        this.createdAt = OffsetDateTime.now();
    }
}
