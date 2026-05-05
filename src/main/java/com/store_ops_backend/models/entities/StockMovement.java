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

@Table(name = "stock_movements")
@Entity(name = "stock_movements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String type;
    private BigDecimal quantity;
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public StockMovement(StockItem stockItem, Company company, User user,
                         String type, BigDecimal quantity, String notes) {
        this.stockItem = stockItem;
        this.company = company;
        this.user = user;
        this.type = type;
        this.quantity = quantity;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }
}
