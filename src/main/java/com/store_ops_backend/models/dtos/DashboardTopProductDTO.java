package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record DashboardTopProductDTO(
    String name,
    BigDecimal totalQuantity,
    BigDecimal totalRevenue
) {}
