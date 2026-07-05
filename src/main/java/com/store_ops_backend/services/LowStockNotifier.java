package com.store_ops_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.store_ops_backend.models.dtos.NotificationEventDTO;
import com.store_ops_backend.models.entities.StockItem;

@Component
public class LowStockNotifier {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public boolean isBelow(StockItem item) {
        return item.getMinQuantity() != null
            && item.getMinQuantity().signum() > 0
            && item.getQuantity().compareTo(item.getMinQuantity()) < 0;
    }

    /**
     * Publica LOW_STOCK apenas quando o item CRUZA o mínimo (não estava abaixo e passou a estar),
     * evitando uma notificação a cada venda.
     */
    public void notifyIfCrossedMinimum(StockItem item, boolean wasBelow) {
        try {
            if (wasBelow || !isBelow(item)) {
                return;
            }
            eventPublisher.publishEvent(new NotificationEventDTO(
                item.getCompany().getId(),
                NotificationEventDTO.Type.LOW_STOCK,
                "Estoque baixo: " + itemName(item),
                "Restam " + item.getQuantity().stripTrailingZeros().toPlainString()
                    + " (mínimo: " + item.getMinQuantity().stripTrailingZeros().toPlainString() + ")",
                "/stock",
                "low-stock-" + item.getId(),
                null
            ));
        } catch (Exception ignored) {
            // Notificação é best-effort; nunca afeta a operação principal
        }
    }

    private String itemName(StockItem item) {
        String name = item.getProduct().getName();
        if (item.getVariant() != null) {
            return name + " (" + item.getVariant().getName() + ")";
        }
        if (item.getComponentOption() != null) {
            return name + " (" + item.getComponentOption().getName() + ")";
        }
        return name;
    }
}
