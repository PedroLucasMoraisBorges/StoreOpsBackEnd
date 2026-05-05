package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public record AddVariantDTO(
    @NotBlank String name,
    BigDecimal priceDelta
) {}
