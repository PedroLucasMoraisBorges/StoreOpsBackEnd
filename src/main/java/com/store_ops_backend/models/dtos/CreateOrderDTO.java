package com.store_ops_backend.models.dtos;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderDTO(
    String customerId,
    String customerName,
    String attendantUserId,
    @NotBlank(message = "Tipo do pedido é obrigatório") String type,
    @NotNull(message = "Data de agendamento é obrigatória") OffsetDateTime scheduledAt,
    String deliveryAddress,
    String notes,
    @NotEmpty(message = "O pedido deve ter pelo menos um item") @Valid List<OrderItemDTO> products
) {}
