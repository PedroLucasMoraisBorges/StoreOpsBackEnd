package com.store_ops_backend.models.dtos;

import java.time.OffsetDateTime;
import java.util.List;

public record UpdateOrderDTO(
    String customerId,
    String customerName,
    String attendantUserId,
    String type,
    OffsetDateTime scheduledAt,
    String deliveryAddress,
    String notes,
    List<OrderItemDTO> products
) {}
