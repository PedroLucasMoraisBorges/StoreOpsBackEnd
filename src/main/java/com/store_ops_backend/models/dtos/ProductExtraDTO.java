package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record ProductExtraDTO(String id, String name, BigDecimal price, Boolean active) {}
