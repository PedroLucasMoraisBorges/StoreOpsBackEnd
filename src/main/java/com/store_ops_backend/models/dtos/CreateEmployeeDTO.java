package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateEmployeeDTO(
    @NotBlank(message = "Nome é obrigatório") String name,
    String contact,
    String position,
    String role
) {}
