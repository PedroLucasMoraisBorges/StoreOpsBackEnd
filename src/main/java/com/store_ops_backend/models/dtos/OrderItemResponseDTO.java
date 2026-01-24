package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
    String id,
    String name,
    BigDecimal quantity,
    String unit,
    BigDecimal unitPrice
) {}
