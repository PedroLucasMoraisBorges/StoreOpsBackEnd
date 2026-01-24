package com.store_ops_backend.models.dtos;

import java.util.List;

public record AddOrderItemsDTO(
    List<OrderItemDTO> products
) {}
