package com.fooddelivery.order_tracking_service.model;

import java.time.LocalDateTime;

public class DeliveryLocation {
    private String orderId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String driverId;

    // Constructors
    public DeliveryLocation() {
        this.timestamp = LocalDateTime.now();
    }

    public DeliveryLocation(String orderId, Double latitude, Double longitude, String driverId) {
        this();
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.driverId = driverId;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
}
