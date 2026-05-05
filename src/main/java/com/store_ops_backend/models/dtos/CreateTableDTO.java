package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateTableDTO(
    @NotNull(message = "Número da mesa é obrigatório") @Min(value = 1, message = "Número deve ser >= 1") Integer number,
    String sector,
    Integer capacity
) {}
