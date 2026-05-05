package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record NewOrderNotificationDTO(
        String type,
        String orderId,
        String customerName,
        BigDecimal total,
        String deliveryMode,
        String timestamp) {
}
