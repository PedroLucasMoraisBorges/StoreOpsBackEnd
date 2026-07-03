package com.store_ops_backend.models.dtos;

import java.util.List;

public record CloseSessionDTO(
    List<PaymentSplitDTO> payments,
    String notes
) {}
