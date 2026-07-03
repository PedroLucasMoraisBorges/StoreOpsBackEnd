package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;

public record PaymentSplitDTO(
    String paymentMethodId,
    BigDecimal amount
) {}
