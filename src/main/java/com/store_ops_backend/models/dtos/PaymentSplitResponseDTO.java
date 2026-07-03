package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentSplitResponseDTO(
    String paymentMethodId,
    String paymentMethodName,
    BigDecimal amount,
    OffsetDateTime paidAt
) {}
