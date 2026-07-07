package com.store_ops_backend.models.dtos;

public record UpdateEmployeeDTO(
    String name,
    String contact,
    String role,
    String position,
    Boolean status
) {}
