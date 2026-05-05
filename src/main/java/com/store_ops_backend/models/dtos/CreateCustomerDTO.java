package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerDTO(
    @NotBlank(message = "Nome do cliente é obrigatório") String name,
    String address,
    String contact,
    Boolean isActive
) {}
