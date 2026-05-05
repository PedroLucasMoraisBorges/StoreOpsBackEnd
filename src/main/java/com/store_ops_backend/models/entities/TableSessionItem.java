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

@Table(name = "table_session_items")
@Entity(name = "table_session_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TableSessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private BigDecimal quantity;
    private String unit;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "added_at")
    private OffsetDateTime addedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private TableSession session;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    public TableSessionItem(TableSession session, String name, BigDecimal quantity, String unit, BigDecimal unitPrice) {
        this.session = session;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.addedAt = OffsetDateTime.now();
    }

    public TableSessionItem(TableSession session, String name, BigDecimal quantity, String unit, BigDecimal unitPrice,
                            Product product, ProductVariant variant) {
        this(session, name, quantity, unit, unitPrice);
        this.product = product;
        this.variant = variant;
    }
}
