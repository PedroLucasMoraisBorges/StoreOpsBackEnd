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

@Table(name = "products")
@Entity(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String category;
    private String unit;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "sell_price")
    private BigDecimal sellPrice;

    private Boolean active;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public Product(Company company, String name, String category, String unit,
                   BigDecimal costPrice, BigDecimal sellPrice) {
        this.company = company;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.costPrice = costPrice;
        this.sellPrice = sellPrice;
        this.active = true;
        this.createdAt = OffsetDateTime.now();
    }

    public void update(String name, String category, String unit,
                       BigDecimal costPrice, BigDecimal sellPrice, Boolean active) {
        if (name != null && !name.isBlank()) this.name = name;
        if (category != null) this.category = category;
        if (unit != null && !unit.isBlank()) this.unit = unit;
        if (costPrice != null) this.costPrice = costPrice;
        if (sellPrice != null) this.sellPrice = sellPrice;
        if (active != null) this.active = active;
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
