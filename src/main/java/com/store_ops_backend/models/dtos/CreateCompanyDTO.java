package com.store_ops_backend.models.dtos;

import java.util.List;

public record CreateCompanyDTO(
    String name,
    String type,
    String address,
    String phone,
    String teamSize,
    String formOfService,

    List<String> paymentMethodIds,

    NotificationSettingsDTO notifications
) {}
