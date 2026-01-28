package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record DashboardDailySummaryDTO(
    int entriesCount,
    BigDecimal entriesTotal,
    int outputsCount,
    BigDecimal outputsTotal,
    BigDecimal balance
) {}
