package com.store_ops_backend.models.entities;

import java.io.Serializable;
import java.time.OffsetDateTime;

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

@Table(name = "user_companies")
@Entity(name = "UserCompany")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserCompany {

    @EmbeddedId
    private UserCompanyId id;

    @Column(nullable = false)
    private String role;

    private Boolean status;

    private String position;

    private OffsetDateTime joined_at;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public UserCompany(User user, Company company, String role, String position) {
        this.user = user;
        this.company = company;
        this.role = role;
        this.position = position;
        this.status = true;
        this.joined_at = OffsetDateTime.now();
        this.id = new UserCompanyId(user.getId(), company.getId());
    }

    public void update(String role, String position, Boolean status) {
        if (role != null && !role.isBlank()) {
            this.role = role;
        }
        if (position != null && !position.isBlank()) {
            this.position = position;
        }
        if (status != null) {
            this.status = status;
        }
    }
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
