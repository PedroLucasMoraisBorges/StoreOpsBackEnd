package com.store_ops_backend.models.dtos;

import java.util.List;

public record ProductComponentGroupDTO(
    String id,
    String name,
    int maxSelections,
    boolean required,
    Boolean active,
    List<ProductComponentOptionDTO> options
) {}
