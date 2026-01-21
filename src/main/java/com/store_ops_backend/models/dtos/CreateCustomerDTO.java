package com.store_ops_backend.models.dtos;

public record CreateCustomerDTO(
    String name,
    String address,
    String contact,
    Boolean isActive
) {}
