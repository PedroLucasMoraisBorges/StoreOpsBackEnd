package com.store_ops_backend.models.dtos;

import java.util.List;

import com.store_ops_backend.models.entities.Company;

public record CompanyResponseDTO(
    Company establishment,
    List<PaymentMethodDTO> paymentMethods,
    List<PaymentMethodDTO> allPaymentMethods
) {}
