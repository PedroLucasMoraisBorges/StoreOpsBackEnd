package com.store_ops_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.store_ops_backend.services.OrderNotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private OrderNotificationService notificationService;

    @GetMapping(value = "/orders/subscribe/{companyId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeOrders(@PathVariable String companyId) {
        return notificationService.subscribe(companyId);
    }
}
