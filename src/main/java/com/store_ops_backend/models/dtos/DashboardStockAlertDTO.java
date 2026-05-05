package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record DashboardStockAlertDTO(
    String productId,
    String productName,
    String unit,
    BigDecimal quantity,
    BigDecimal minQuantity
) {}
