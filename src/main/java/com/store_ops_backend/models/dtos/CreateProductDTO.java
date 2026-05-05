package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record CreateProductDTO(
    @NotBlank(message = "Nome do produto é obrigatório") String name,
    String category,
    @NotBlank(message = "Unidade é obrigatória") String unit,
    @DecimalMin(value = "0.00", inclusive = true, message = "Preço de venda inválido") BigDecimal sellPrice
) {}
