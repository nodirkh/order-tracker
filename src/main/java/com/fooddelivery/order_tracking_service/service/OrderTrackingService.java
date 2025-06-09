package com.fooddelivery.order_tracking_service.service;

import com.fooddelivery.order_tracking_service.dto.CreateOrderRequest;
import com.fooddelivery.order_tracking_service.dto.OrderTrackingResponse;
import com.fooddelivery.order_tracking_service.model.*;
import com.fooddelivery.order_tracking_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderTrackingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCATION_KEY_PREFIX = "location:";

    public Order createOrder(CreateOrderRequest request) {
        String orderNumber = generateOrderNumber();

        Order order = new Order(orderNumber, request.getCustomerId(),
                request.getRestaurantId(), request.getDeliveryAddress());

        // Add initial status history
        OrderStatusHistory initialHistory = new OrderStatusHistory(order, OrderStatus.PLACED, "Order placed successfully");
        order.getStatusHistory().add(initialHistory);

        Order savedOrder = orderRepository.save(order);

        // Notify via WebSocket
        notifyOrderUpdate(savedOrder);

        return savedOrder;
    }

    public Optional<OrderTrackingResponse> getOrderTracking(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::convertToTrackingResponse);
    }

    public Order updateOrderStatus(String orderNumber, OrderStatus newStatus, String notes) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        if (order.getStatus() != newStatus) {
            order.setStatus(newStatus);

            // Add status history
            OrderStatusHistory statusHistory = new OrderStatusHistory(order, newStatus, notes);
            order.getStatusHistory().add(statusHistory);

            // Update estimated delivery time based on status
            updateEstimatedDeliveryTime(order, newStatus);

            Order savedOrder = orderRepository.save(order);

            // Notify via WebSocket
            notifyOrderUpdate(savedOrder);

            return savedOrder;
        }

        return order;
    }

    public void updateDeliveryLocation(String orderNumber, Double latitude, Double longitude, String driverId) {
        DeliveryLocation location = new DeliveryLocation(orderNumber, latitude, longitude, driverId);

        // Store in Redis for fast access
        redisTemplate.opsForValue().set(LOCATION_KEY_PREFIX + orderNumber, location);

        // Notify via WebSocket
        messagingTemplate.convertAndSend("/topic/location/" + orderNumber, location);
    }

    public Optional<DeliveryLocation> getCurrentLocation(String orderNumber) {
        DeliveryLocation location = (DeliveryLocation) redisTemplate.opsForValue()
                .get(LOCATION_KEY_PREFIX + orderNumber);
        return Optional.ofNullable(location);
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> getActiveOrders() {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY
        );
        return orderRepository.findByStatusIn(activeStatuses);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void updateEstimatedDeliveryTime(Order order, OrderStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED -> order.setEstimatedDeliveryTime(now.plusMinutes(25));
            case PREPARING -> order.setEstimatedDeliveryTime(now.plusMinutes(20));
            case READY_FOR_PICKUP -> order.setEstimatedDeliveryTime(now.plusMinutes(15));
            case PICKED_UP -> order.setEstimatedDeliveryTime(now.plusMinutes(10));
            case OUT_FOR_DELIVERY -> order.setEstimatedDeliveryTime(now.plusMinutes(5));
        }
    }

    private OrderTrackingResponse convertToTrackingResponse(Order order) {
        OrderTrackingResponse response = new OrderTrackingResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setCurrentStatus(order.getStatus());
        response.setStatusDescription(order.getStatus().getDescription());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setDeliveryAddress(order.getDeliveryAddress());

        // Get current location from Redis
        getCurrentLocation(order.getOrderNumber()).ifPresent(response::setCurrentLocation);

        // Convert status history
        List<OrderTrackingResponse.StatusUpdate> statusUpdates = order.getStatusHistory().stream()
                .map(history -> new OrderTrackingResponse.StatusUpdate(
                        history.getStatus(),
                        history.getStatus().getDescription(),
                        history.getTimestamp(),
                        history.getNotes()
                ))
                .collect(Collectors.toList());

        response.setStatusHistory(statusUpdates);

        return response;
    }

    private void notifyOrderUpdate(Order order) {
        OrderTrackingResponse response = convertToTrackingResponse(order);

        // Notify customer
        messagingTemplate.convertAndSend("/topic/order/" + order.getOrderNumber(), response);

        // Notify customer's personal channel
        messagingTemplate.convertAndSend("/topic/customer/" + order.getCustomerId(), response);

        // Notify driver if assigned
        if (order.getDeliveryDriverId() != null) {
            messagingTemplate.convertAndSend("/topic/driver/" + order.getDeliveryDriverId(), response);
        }
    }
}