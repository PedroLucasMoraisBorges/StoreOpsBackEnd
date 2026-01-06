package com.store_ops_backend.models.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users_companies")
@Entity(name = "users_companies")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserCompany {

    @EmbeddedId
    private UserCompanyId id;

    @Column(nullable = false)
    private String role;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode 
class UserCompanyId implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "company_id")
    private String companyId;
}