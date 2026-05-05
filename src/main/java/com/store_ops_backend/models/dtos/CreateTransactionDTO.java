package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateTransactionDTO(
    @NotNull(message = "Valor é obrigatório") @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero") BigDecimal amount,
    String description
) {}
