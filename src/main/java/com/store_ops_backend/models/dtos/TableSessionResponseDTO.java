package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record TableSessionResponseDTO(
    String id,
    Integer tableNumber,
    String tableSector,
    String status,
    String notes,
    OffsetDateTime openedAt,
    OffsetDateTime closedAt,
    List<SessionItemResponseDTO> items,
    BigDecimal total,
    String paymentMethodId,
    String paymentMethodName,
    OffsetDateTime paidAt
) {}
