package com.fooddelivery.order_tracking_service.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/subscribe")
    @SendTo("/topic/notifications")
    public String subscribeToNotifications(String message) {
        return "Subscribed to notifications: " + message;
    }
}