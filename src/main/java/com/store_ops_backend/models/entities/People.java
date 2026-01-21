package com.store_ops_backend.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "people")
@Entity(name = "people")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class People {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String type;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String address;
    private String contact;

    @Column(name = "is_active")
    private Boolean is_active;

    public People(String name, String type, Company company, User user) {
        this.name = name;
        this.type = type;
        this.company = company;
        this.user = user;
        this.is_active = true;
    }

    public People(
        String name,
        String type,
        Company company,
        User user,
        String address,
        String contact,
        Boolean isActive
    ) {
        this.name = name;
        this.type = type;
        this.company = company;
        this.user = user;
        this.address = address;
        this.contact = contact;
        this.is_active = isActive == null ? true : isActive;
    }

    public void update(String name, String address, String contact, Boolean isActive) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (address != null) {
            this.address = address;
        }
        if (contact != null) {
            this.contact = contact;
        }
        if (isActive != null) {
            this.is_active = isActive;
        }
    }
}
