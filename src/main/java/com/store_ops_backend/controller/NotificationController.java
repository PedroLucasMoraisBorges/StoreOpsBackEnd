package com.store_ops_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.store_ops_backend.infra.config.VapidProperties;
import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.dtos.PushSubscriptionDTO;
import com.store_ops_backend.models.dtos.UnsubscribePushDTO;
import com.store_ops_backend.models.dtos.UserNotificationPreferencesDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.OrderNotificationService;
import com.store_ops_backend.services.PushSubscriptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private OrderNotificationService notificationService;

    @Autowired
    private PushSubscriptionService pushSubscriptionService;

    @Autowired
    private AuthorizationHelper authorizationHelper;

    @Autowired
    private VapidProperties vapidProperties;

    @GetMapping(value = "/orders/subscribe/{companyId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeOrders(@PathVariable String companyId) {
        return notificationService.subscribe(companyId);
    }

    @GetMapping("/push/vapid-public-key")
    public Map<String, String> getVapidPublicKey() {
        return Map.of("publicKey", vapidProperties.getPublicKey());
    }

    @PostMapping("/push/subscribe/{companyId}")
    public void subscribePush(
        @PathVariable("companyId") String companyId,
        @RequestBody @Valid PushSubscriptionDTO data,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        pushSubscriptionService.subscribe(user, companyId, data);
    }

    @PostMapping("/push/unsubscribe/{companyId}")
    public void unsubscribePush(
        @PathVariable("companyId") String companyId,
        @RequestBody @Valid UnsubscribePushDTO data,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        pushSubscriptionService.unsubscribe(user, companyId, data.endpoint());
    }

    @GetMapping("/preferences/{companyId}")
    public UserNotificationPreferencesDTO getPreferences(
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return pushSubscriptionService.getPreferences(user, companyId);
    }

    @PutMapping("/preferences/{companyId}")
    public UserNotificationPreferencesDTO updatePreferences(
        @PathVariable("companyId") String companyId,
        @RequestBody UserNotificationPreferencesDTO data,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return pushSubscriptionService.updatePreferences(user, companyId, data);
    }
}
