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

@Table(name = "company_payment_methods")
@Entity(name = "company_payment_methods")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CompanyPaymentMethods {
    @EmbeddedId
    private CompanyPaymentMethodsId id;

    @ManyToOne(optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(optional = false)
    @MapsId("paymentMethodId")
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethods paymentMethod;

    public CompanyPaymentMethods(Company company, PaymentMethods paymentMethod) {
        this.company = company;
        this.paymentMethod = paymentMethod;
        this.id = new CompanyPaymentMethodsId(company.getId(), paymentMethod.getId());
    }
}

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode 
class CompanyPaymentMethodsId implements Serializable {

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "payment_method_id")
    private String paymentMethodId;
}