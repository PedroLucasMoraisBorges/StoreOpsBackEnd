package com.store_ops_backend.models.dtos;

public record PaymentMethodDTO(
    String id,
    String name,
    String code
) {
}
