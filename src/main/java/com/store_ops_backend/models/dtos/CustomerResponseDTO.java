package com.store_ops_backend.models.dtos;

public record CustomerResponseDTO(
    String id,
    String name,
    String address,
    String contact,
    Boolean isActive
) {}
