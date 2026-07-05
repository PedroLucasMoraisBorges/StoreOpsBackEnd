package com.store_ops_backend.services;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.store_ops_backend.repositories.PushSubscriptionRepository;

@Component
public class PushSubscriptionCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PushSubscriptionCleanupJob.class);
    private static final int STALE_DAYS = 90;

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    // Complemento à limpeza oportunista (410 no envio). Em Cloud Run com
    // scale-to-zero o job pode não rodar — a limpeza oportunista cobre o resto.
    @Scheduled(cron = "0 0 4 * * *")
    public void removeStaleSubscriptions() {
        try {
            int removed = pushSubscriptionRepository.deleteStale(OffsetDateTime.now().minusDays(STALE_DAYS));
            if (removed > 0) {
                log.info("[Push] Limpeza: {} subscriptions inativas há mais de {} dias removidas", removed, STALE_DAYS);
            }
        } catch (Exception e) {
            log.warn("[Push] Falha na limpeza de subscriptions: {}", e.getMessage());
        }
    }
}
