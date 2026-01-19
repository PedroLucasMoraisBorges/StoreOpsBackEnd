package com.store_ops_backend.models.dtos;

public record UpdateEmployeeDTO(
    String name,
    String role,
    String position,
    Boolean status
) {}
