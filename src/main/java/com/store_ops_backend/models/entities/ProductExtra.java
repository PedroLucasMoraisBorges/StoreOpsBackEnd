package com.store_ops_backend.models.entities;

import java.math.BigDecimal;

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

@Table(name = "product_extras")
@Entity(name = "product_extras")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private BigDecimal price;

    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductExtra(Product product, String name, BigDecimal price) {
        this.product = product;
        this.name = name;
        this.price = price;
        this.active = true;
    }
}
