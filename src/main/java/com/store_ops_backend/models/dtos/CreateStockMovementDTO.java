package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateStockMovementDTO(
    String productId,
    String variantId,
    String componentOptionId,
    @NotBlank(message = "Tipo de movimentação é obrigatório")
    @Pattern(regexp = "ENTRADA|SAIDA|AJUSTE", message = "Tipo deve ser ENTRADA, SAIDA ou AJUSTE") String type,
    @NotNull @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantity,
    String notes
) {}
