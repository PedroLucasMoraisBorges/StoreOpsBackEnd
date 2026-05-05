package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record UpdateProductDTO(
    String name,
    String category,
    String unit,
    BigDecimal sellPrice,
    Boolean active
) {}
