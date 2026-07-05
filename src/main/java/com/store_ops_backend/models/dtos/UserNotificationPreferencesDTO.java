package com.store_ops_backend.models.dtos;

public record UserNotificationPreferencesDTO(
    Boolean newOrder,
    Boolean accounts,
    Boolean cashRegister,
    Boolean lowStock,
    Boolean weeklyReports,
    Boolean email
) {}
