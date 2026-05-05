package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddSessionItemDTO(
    @NotBlank(message = "Nome do item é obrigatório") String name,
    @NotNull @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero") BigDecimal quantity,
    String unit,
    @NotNull @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero") BigDecimal unitPrice,
    String productId,
    String variantId
) {}
