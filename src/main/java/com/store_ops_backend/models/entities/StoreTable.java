package com.store_ops_backend.models.entities;

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

@Table(name = "store_tables")
@Entity(name = "store_tables")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class StoreTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer number;
    private String sector;
    private Integer capacity;
    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public StoreTable(Company company, Integer number, String sector, Integer capacity) {
        this.company = company;
        this.number = number;
        this.sector = sector;
        this.capacity = capacity;
        this.status = "FREE";
        this.createdAt = OffsetDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void update(String sector, Integer capacity) {
        if (sector != null) this.sector = sector;
        if (capacity != null) this.capacity = capacity;
    }
}
