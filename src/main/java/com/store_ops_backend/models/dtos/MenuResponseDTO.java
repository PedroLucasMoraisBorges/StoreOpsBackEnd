package com.store_ops_backend.models.dtos;

import java.util.List;

public record MenuResponseDTO(
    String companyId,
    String companyName,
    String phone,
    String address,
    List<ProductResponseDTO> products,
    List<PaymentMethodDTO> paymentMethods,
    boolean isOpen
) {
    public record PaymentMethodDTO(String id, String name, String code) {}
}
