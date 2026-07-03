package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record ProductProfitabilityDTO(
    String productId,
    String productName,
    String category,
    BigDecimal totalQuantitySold,
    BigDecimal totalRevenue,
    BigDecimal totalCost,
    BigDecimal grossMargin,
    BigDecimal marginPercent
) {}
