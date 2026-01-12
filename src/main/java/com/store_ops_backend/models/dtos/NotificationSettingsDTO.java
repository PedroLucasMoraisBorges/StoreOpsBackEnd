package com.store_ops_backend.models.dtos;

public record NotificationSettingsDTO(
    Boolean newOrder,
    Boolean weeklyReports,
    Boolean email,
    Boolean accounts
) {}

