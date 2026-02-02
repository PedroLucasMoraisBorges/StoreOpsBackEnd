package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DashboardRecentOrderDTO(
    String id,
    String customerName,
    String status,
    BigDecimal total,
    OffsetDateTime createdAt,
    OffsetDateTime scheduledAt
) {}
