package com.store_ops_backend.services;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.store_ops_backend.models.dtos.NotificationEventDTO;
import com.store_ops_backend.models.entities.PushSubscription;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.models.entities.UserNotificationPreference;
import com.store_ops_backend.repositories.PushSubscriptionRepository;
import com.store_ops_backend.repositories.UserCompanyRepository;
import com.store_ops_backend.repositories.UserNotificationPreferenceRepository;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Autowired
    private UserNotificationPreferenceRepository preferenceRepository;

    @Autowired
    private WebPushService webPushService;

    // AFTER_COMMIT: nunca notifica transação que sofreu rollback.
    // fallbackExecution: MenuController.placeOrder não é transacional.
    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotificationEvent(NotificationEventDTO event) {
        try {
            dispatch(event);
        } catch (Exception e) {
            log.warn("[Push] Falha no dispatch do evento {}: {}", event.type(), e.getMessage());
        }
    }

    private void dispatch(NotificationEventDTO event) {
        if (!webPushService.isEnabled()) {
            return;
        }

        List<String> eligibleUserIds = userCompanyRepository.findUsersByCompanyId(event.companyId())
            .stream()
            .filter(uc -> uc.getStatus() == null || Boolean.TRUE.equals(uc.getStatus()))
            .filter(uc -> roleAllows(uc.getRole(), event.type()))
            .map(uc -> uc.getUser().getId())
            .filter(userId -> !userId.equals(event.actorUserId()))
            .toList();

        if (eligibleUserIds.isEmpty()) {
            return;
        }

        Map<String, UserNotificationPreference> prefs = preferenceRepository
            .findByCompanyIdAndUserIdIn(event.companyId(), eligibleUserIds)
            .stream()
            .collect(Collectors.toMap(UserNotificationPreference::getUserId, Function.identity()));

        List<String> recipients = eligibleUserIds.stream()
            .filter(userId -> preferenceAllows(prefs.get(userId), event.type()))
            .toList();

        if (recipients.isEmpty()) {
            return;
        }

        List<PushSubscription> subscriptions =
            pushSubscriptionRepository.findByCompanyIdAndUserIdIn(event.companyId(), recipients);

        Map<String, String> payload = Map.of(
            "type", event.type().name(),
            "title", event.title() != null ? event.title() : "StoreOps",
            "body", event.body() != null ? event.body() : "",
            "url", event.url() != null ? event.url() : "/",
            "tag", event.tag() != null ? event.tag() : event.type().name()
        );

        int sent = 0, failed = 0, removed = 0;
        for (PushSubscription subscription : subscriptions) {
            switch (webPushService.send(subscription, payload)) {
                case SENT -> {
                    sent++;
                    try {
                        subscription.markSuccess();
                        pushSubscriptionRepository.save(subscription);
                    } catch (Exception ignored) { }
                }
                case GONE -> {
                    removed++;
                    try {
                        pushSubscriptionRepository.delete(subscription);
                    } catch (Exception ignored) { }
                }
                case FAILED -> failed++;
                case DISABLED -> { return; }
            }
        }
        log.info("[Push] Evento {} (company={}): enviados={}, falhas={}, removidos={}",
            event.type(), event.companyId(), sent, failed, removed);
    }

    private boolean roleAllows(String role, NotificationEventDTO.Type type) {
        if (type == NotificationEventDTO.Type.NEW_ORDER) {
            return true;
        }
        String r = role != null ? role.toUpperCase() : "";
        return "ADMIN".equals(r) || "MANAGER".equals(r);
    }

    private boolean preferenceAllows(UserNotificationPreference pref, NotificationEventDTO.Type type) {
        if (pref == null) {
            // Sem linha de preferências = defaults (todos os tipos push habilitados)
            return true;
        }
        return switch (type) {
            case NEW_ORDER -> pref.isNewOrder();
            case FIADO_DEBIT, FIADO_PAYMENT -> pref.isAccounts();
            case CASH_OPEN, CASH_CLOSE -> pref.isCashRegister();
            case LOW_STOCK -> pref.isLowStock();
        };
    }
}
