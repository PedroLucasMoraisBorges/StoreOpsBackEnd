package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SessionItemResponseDTO(
    String id,
    String name,
    BigDecimal quantity,
    String unit,
    BigDecimal unitPrice,
    BigDecimal total,
    OffsetDateTime addedAt
) {}
