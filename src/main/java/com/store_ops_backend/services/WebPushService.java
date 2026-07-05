package com.store_ops_backend.services;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store_ops_backend.infra.config.VapidProperties;
import com.store_ops_backend.models.entities.PushSubscription;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Service
public class WebPushService {

    public enum SendResult { SENT, GONE, FAILED, DISABLED }

    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    private final PushService pushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebPushService(VapidProperties vapidProperties) {
        PushService service = null;
        if (vapidProperties.isConfigured()) {
            try {
                if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                service = new PushService(
                    vapidProperties.getPublicKey(),
                    vapidProperties.getPrivateKey(),
                    vapidProperties.getSubject()
                );
                log.info("[WebPush] Serviço de Web Push habilitado (VAPID configurado)");
            } catch (Exception e) {
                log.error("[WebPush] Falha ao inicializar o serviço de push — push desabilitado", e);
                service = null;
            }
        } else {
            log.warn("[WebPush] Chaves VAPID não configuradas — envio de push desabilitado (no-op)");
        }
        this.pushService = service;
    }

    public boolean isEnabled() {
        return pushService != null;
    }

    public SendResult send(PushSubscription subscription, Object payload) {
        if (pushService == null) {
            return SendResult.DISABLED;
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(payload);
            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                body
            );
            var response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();
            if (status == 404 || status == 410) {
                log.info("[WebPush] Subscription expirada (HTTP {}) — será removida", status);
                return SendResult.GONE;
            }
            if (status >= 400) {
                log.warn("[WebPush] Falha ao enviar push (HTTP {})", status);
                return SendResult.FAILED;
            }
            return SendResult.SENT;
        } catch (Exception e) {
            log.warn("[WebPush] Erro ao enviar push: {}", e.getMessage());
            return SendResult.FAILED;
        }
    }
}
