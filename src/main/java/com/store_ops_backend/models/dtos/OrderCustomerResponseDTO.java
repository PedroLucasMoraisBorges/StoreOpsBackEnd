package com.store_ops_backend.models.dtos;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderCustomerResponseDTO(
    String id,
    String attendantUserId,
    String type,
    OffsetDateTime scheduledAt,
    String deliveryAddress,
    String notes,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<OrderItemResponseDTO> products
) {}
