package com.store_ops_backend.models.entities;

import java.math.BigDecimal;

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

@Table(name = "product_variants")
@Entity(name = "product_variants")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(name = "price_delta")
    private BigDecimal priceDelta;

    private Boolean active;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductVariant(Product product, String name, BigDecimal priceDelta) {
        this.product = product;
        this.name = name;
        this.priceDelta = priceDelta;
        this.active = true;
    }
}
