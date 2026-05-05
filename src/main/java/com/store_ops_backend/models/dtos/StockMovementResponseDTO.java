package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StockMovementResponseDTO(
    String id,
    String type,
    BigDecimal quantity,
    String notes,
    String productName,
    String variantName,
    String userId,
    OffsetDateTime createdAt
) {}
