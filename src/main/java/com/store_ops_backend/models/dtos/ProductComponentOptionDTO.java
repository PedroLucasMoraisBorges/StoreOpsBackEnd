package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record ProductComponentOptionDTO(String id, String name, BigDecimal stockQuantity) {}
