package com.fooddelivery.order_tracking_service.dto;

import com.fooddelivery.order_tracking_service.model.DeliveryAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String restaurantId;

    @NotNull
    private DeliveryAddress deliveryAddress;

    // Constructors
    public CreateOrderRequest() {}

    public CreateOrderRequest(String customerId, String restaurantId, DeliveryAddress deliveryAddress) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
    }

    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public DeliveryAddress getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddress deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}