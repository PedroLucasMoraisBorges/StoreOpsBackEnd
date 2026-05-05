package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OnlineOrderTrackingDTO(
    String orderId,
    String status,
    String customerName,
    BigDecimal total,
    OffsetDateTime createdAt,
    List<OrderItemResponseDTO> items,
    String deliveryMode,
    String deliveryAddress,
    String paymentNote
) {}
