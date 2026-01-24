package com.store_ops_backend.models.entities;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "orders")
@Entity(name = "orders")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String type;
    private String status;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_person_id", nullable = false)
    private People customer;

    @Column(name = "attendant_user_id")
    private String attendantUserId;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="attendant_user_id", referencedColumnName="user_id", insertable=false, updatable=false),
        @JoinColumn(name="company_id", referencedColumnName="company_id", insertable=false, updatable=false)
    })
    private UserCompany attendant;


    public Order(
        Company company,
        People customer,
        UserCompany attendant,
        String type,
        String status,
        OffsetDateTime scheduledAt,
        String deliveryAddress,
        String notes
    ) {
        this.company = company;
        this.customer = customer;
        this.attendant = attendant;
        this.attendantUserId = attendant.getUser().getId();
        this.type = type;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void update(
        People customer,
        UserCompany attendant,
        String type,
        OffsetDateTime scheduledAt,
        String deliveryAddress,
        String notes
    ) {
        if (customer != null) {
            this.customer = customer;
        }
        if (attendant != null) {
            this.attendant = attendant;
            this.attendantUserId = attendant.getUser().getId();
        }
        if (type != null && !type.isBlank()) {
            this.type = type;
        }
        if (scheduledAt != null) {
            this.scheduledAt = scheduledAt;
        }
        if (deliveryAddress != null) {
            this.deliveryAddress = deliveryAddress;
        }
        if (notes != null) {
            this.notes = notes;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateStatus(String status) {
        if (status != null && !status.isBlank()) {
            this.status = status;
            this.updatedAt = OffsetDateTime.now();
        }
    }
}
