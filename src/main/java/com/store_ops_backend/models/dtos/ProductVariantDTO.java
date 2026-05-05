package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record ProductVariantDTO(String id, String name, BigDecimal priceDelta, Boolean active, BigDecimal stockQuantity) {}
