package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateCounterSaleDTO(
    @NotEmpty @Valid List<CounterSaleItemDTO> items,
    @NotBlank String paymentMethodId,
    String customerName,
    String notes
) {
    public record CounterSaleItemDTO(
        @NotBlank String name,
        @NotNull @Positive BigDecimal quantity,
        @NotBlank String unit,
        @NotNull @PositiveOrZero BigDecimal unitPrice,
        String productId,
        String variantId,
        String notes
    ) {}
}
