package com.store_ops_backend.models.dtos;

public record UpdateCustomerDTO(
    String name,
    String address,
    String contact,
    Boolean isActive
) {}
