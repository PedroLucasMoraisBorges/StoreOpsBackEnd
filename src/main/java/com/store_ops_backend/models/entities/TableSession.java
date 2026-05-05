package com.store_ops_backend.models.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.store_ops_backend.models.entities.PaymentMethods;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "table_sessions")
@Entity(name = "table_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String status;
    private String notes;

    @Column(name = "opened_at")
    private OffsetDateTime openedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private StoreTable table;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethods paymentMethod;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TableSessionItem> items = new ArrayList<>();

    public TableSession(StoreTable table, Company company, String notes) {
        this.table = table;
        this.company = company;
        this.notes = notes;
        this.status = "OPEN";
        this.openedAt = OffsetDateTime.now();
    }

    public void close() {
        this.status = "CLOSED";
        this.closedAt = OffsetDateTime.now();
    }

    public void closeWithPayment(PaymentMethods paymentMethod) {
        this.status = "CLOSED";
        this.closedAt = OffsetDateTime.now();
        this.paymentMethod = paymentMethod;
        this.paidAt = OffsetDateTime.now();
    }

    public void addItem(TableSessionItem item) {
        this.items.add(item);
    }

    public BigDecimal getTotal() {
        return items.stream()
            .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
