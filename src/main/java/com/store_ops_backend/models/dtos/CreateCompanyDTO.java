package com.store_ops_backend.models.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyDTO(
    @NotBlank(message = "Nome do estabelecimento é obrigatório") String name,
    String type,
    String address,
    String phone,
    String teamSize,
    String formOfService,
    List<String> paymentMethodIds,
    NotificationSettingsDTO notifications
) {}
