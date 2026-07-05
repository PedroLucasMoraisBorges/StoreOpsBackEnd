package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record PushSubscriptionDTO(
    @NotBlank String endpoint,
    @NotBlank String p256dh,
    @NotBlank String auth,
    String userAgent
) {}
