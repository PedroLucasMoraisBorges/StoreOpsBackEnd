package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record DashboardWeeklyRevenueDTO(
    String dayLabel,
    BigDecimal total
) {}
