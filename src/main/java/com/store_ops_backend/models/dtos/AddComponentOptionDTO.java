package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record AddComponentOptionDTO(
    @NotBlank String name
) {}
