package com.store_ops_backend.models.dtos;

public record NotificationEventDTO(
    String companyId,
    Type type,
    String title,
    String body,
    String url,
    String tag,
    String actorUserId
) {
    public enum Type {
        NEW_ORDER,
        FIADO_DEBIT,
        FIADO_PAYMENT,
        CASH_OPEN,
        CASH_CLOSE,
        LOW_STOCK
    }
}
