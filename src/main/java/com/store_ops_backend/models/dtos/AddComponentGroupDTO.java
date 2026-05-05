package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record AddComponentGroupDTO(
    @NotBlank String name,
    int maxSelections,
    boolean required
) {}
