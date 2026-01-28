package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record DashboardTopCustomerDTO(
    String id,
    String name,
    BigDecimal balance
) {}
