package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record TopProductRawDTO(
    String name,
    BigDecimal totalQuantity,
    BigDecimal totalRevenue
) {}
