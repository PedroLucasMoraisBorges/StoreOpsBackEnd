package com.store_ops_backend.models.dtos;

import java.time.OffsetDateTime;

public record EmployeeResponseDTO(
    String userId,
    String name,
    String login,
    String role,
    String position,
    Boolean status,
    OffsetDateTime joinedAt
) {}
