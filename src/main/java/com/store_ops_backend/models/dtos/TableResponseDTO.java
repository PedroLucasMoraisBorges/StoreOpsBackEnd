package com.store_ops_backend.models.dtos;

public record TableResponseDTO(
    String id,
    Integer number,
    String sector,
    Integer capacity,
    String status
) {}
