package com.store_ops_backend.models.entities;

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

@Table(name = "product_component_groups")
@Entity(name = "product_component_groups")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductComponentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(name = "max_selections")
    private int maxSelections;

    private boolean required;

    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductComponentGroup(Product product, String name, int maxSelections, boolean required) {
        this.product = product;
        this.name = name;
        this.maxSelections = maxSelections;
        this.required = required;
        this.active = true;
    }
}
