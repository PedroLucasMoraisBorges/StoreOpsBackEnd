package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ProductResponseDTO(
    String id,
    String name,
    String category,
    String unit,
    BigDecimal costPrice,
    BigDecimal sellPrice,
    Boolean active,
    String imageUrl,
    OffsetDateTime createdAt,
    List<ProductVariantDTO> variants,
    List<ProductExtraDTO> extras,
    List<ProductComponentGroupDTO> componentGroups,
    BigDecimal stockQuantity
) {}
