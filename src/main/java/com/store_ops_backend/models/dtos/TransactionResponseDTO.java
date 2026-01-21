package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponseDTO(
    String id,
    String origin,
    BigDecimal amount,
    String description,
    OffsetDateTime createdAt,
    String userId
) {}
