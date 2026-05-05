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

@Table(name = "stock_items")
@Entity(name = "stock_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private BigDecimal quantity;

    @Column(name = "min_quantity")
    private BigDecimal minQuantity;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "component_option_id")
    private ProductComponentOption componentOption;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public StockItem(Product product, ProductVariant variant, Company company, BigDecimal minQuantity) {
        this.product = product;
        this.variant = variant;
        this.company = company;
        this.quantity = BigDecimal.ZERO;
        this.minQuantity = minQuantity;
        this.updatedAt = OffsetDateTime.now();
    }

    public StockItem(Product product, ProductComponentOption componentOption, Company company, BigDecimal minQuantity) {
        this.product = product;
        this.componentOption = componentOption;
        this.company = company;
        this.quantity = BigDecimal.ZERO;
        this.minQuantity = minQuantity;
        this.updatedAt = OffsetDateTime.now();
    }

    public void applyMovement(BigDecimal delta) {
        this.quantity = this.quantity.add(delta);
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateMinQuantity(BigDecimal minQuantity) {
        this.minQuantity = minQuantity;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isBelowMinimum() {
        return this.quantity.compareTo(this.minQuantity) < 0;
    }
}
