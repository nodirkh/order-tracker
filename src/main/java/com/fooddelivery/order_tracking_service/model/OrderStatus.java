package com.fooddelivery.order_tracking_service.model;

public enum OrderStatus {
    PLACED("Order placed"),
    CONFIRMED("Order confirmed by restaurant"),
    PREPARING("Being prepared"),
    READY_FOR_PICKUP("Ready for pickup"),
    PICKED_UP("Picked up by driver"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}