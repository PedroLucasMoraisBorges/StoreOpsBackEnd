package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExpenseResponseDTO(
    String id,
    BigDecimal amount,
    String description,
    String category,
    OffsetDateTime expenseDate,
    OffsetDateTime createdAt
) {}
