package com.store_ops_backend.models.dtos;

import java.time.OffsetDateTime;

public record CashRegisterResponseDTO(
    String id,
    String companyId,
    String userId,
    String userDisplayName,
    String shift,
    String status,
    OffsetDateTime openedAt,
    OffsetDateTime closedAt,
    String notes
) {}
