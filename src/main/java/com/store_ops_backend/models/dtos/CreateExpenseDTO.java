package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateExpenseDTO(
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    BigDecimal amount,

    @NotBlank(message = "Descrição é obrigatória")
    String description,

    String category,
    OffsetDateTime expenseDate
) {}
