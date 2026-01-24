package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record OrderItemDTO(
    String name,
    BigDecimal quantity,
    String unit,
    BigDecimal unitPrice
) {}
