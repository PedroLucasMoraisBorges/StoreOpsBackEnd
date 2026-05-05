package com.store_ops_backend.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.store_ops_backend.models.dtos.NewOrderNotificationDTO;

@Service
public class OrderNotificationService {

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String companyId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(companyId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(companyId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("ok"));
        } catch (IOException ignored) { }

        return emitter;
    }

    public void notifyNewOrder(String companyId, String orderId, String customerName,
            BigDecimal total, String deliveryMode) {
        List<SseEmitter> list = emitters.get(companyId);
        System.out.println("[SSE] notifyNewOrder → companyId=" + companyId
                + " | emitters=" + (list != null ? list.size() : 0));
        if (list == null || list.isEmpty()) return;

        NewOrderNotificationDTO payload = new NewOrderNotificationDTO(
                "NEW_ORDER", orderId, customerName, total, deliveryMode,
                OffsetDateTime.now().toString());

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NEW_ORDER")
                        .data(payload, MediaType.APPLICATION_JSON));
                System.out.println("[SSE] Evento NEW_ORDER enviado para emitter");
            } catch (Exception e) {
                System.out.println("[SSE] Emitter morto, removendo: " + e.getMessage());
                dead.add(emitter);
            }
        }
        list.removeAll(dead);
    }

    private void removeEmitter(String companyId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(companyId);
        if (list != null) list.remove(emitter);
    }
}
