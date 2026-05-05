package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StockItemResponseDTO(
    String id,
    String productId,
    String productName,
    String productCategory,
    String unit,
    BigDecimal quantity,
    BigDecimal minQuantity,
    boolean belowMinimum,
    BigDecimal sellPrice,
    OffsetDateTime updatedAt,
    String variantId,
    String variantName,
    String componentOptionId,
    String componentOptionName
) {}
