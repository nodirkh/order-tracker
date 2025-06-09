package com.fooddelivery.order_tracking_service.dto;

import com.fooddelivery.order_tracking_service.model.DeliveryAddress;
import com.fooddelivery.order_tracking_service.model.DeliveryLocation;
import com.fooddelivery.order_tracking_service.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public class OrderTrackingResponse {
    private String orderNumber;
    private OrderStatus currentStatus;
    private String statusDescription;
    private LocalDateTime estimatedDeliveryTime;
    private DeliveryAddress deliveryAddress;
    private DeliveryLocation currentLocation;
    private List<StatusUpdate> statusHistory;

    // Constructors
    public OrderTrackingResponse() {}

    // Getters and Setters
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public OrderStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(OrderStatus currentStatus) { this.currentStatus = currentStatus; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public DeliveryAddress getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddress deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public DeliveryLocation getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(DeliveryLocation currentLocation) { this.currentLocation = currentLocation; }

    public List<StatusUpdate> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusUpdate> statusHistory) { this.statusHistory = statusHistory; }

    public static class StatusUpdate {
        private OrderStatus status;
        private String description;
        private LocalDateTime timestamp;
        private String notes;

        public StatusUpdate() {}

        public StatusUpdate(OrderStatus status, String description, LocalDateTime timestamp, String notes) {
            this.status = status;
            this.description = description;
            this.timestamp = timestamp;
            this.notes = notes;
        }

        // Getters and Setters
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}