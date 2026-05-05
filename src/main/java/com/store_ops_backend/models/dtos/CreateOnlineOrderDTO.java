package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOnlineOrderDTO(
    @NotBlank(message = "Nome do cliente é obrigatório") String customerName,
    String notes,
    // PICKUP or DELIVERY — defaults to PICKUP when null
    String deliveryMode,
    String deliveryAddress,
    String paymentNote,
    @NotEmpty(message = "O pedido deve ter pelo menos um item") @Valid List<CreateOnlineOrderItemDTO> items
) {
    public record CreateOnlineOrderItemDTO(
        @NotBlank String name,
        @NotNull @Positive BigDecimal quantity,
        String unit,
        @NotNull @Positive BigDecimal unitPrice,
        String notes,
        String productId,
        String variantId
    ) {}
}
