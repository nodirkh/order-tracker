package com.fooddelivery.order_tracking_service.service;

import com.fooddelivery.order_tracking_service.model.Order;
import com.fooddelivery.order_tracking_service.model.OrderStatus;
import com.fooddelivery.order_tracking_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class DeliverySimulationService {

    @Autowired
    private OrderTrackingService orderTrackingService;

    @Autowired
    private OrderRepository orderRepository;

    private final Random random = new Random();

    // Simulate every 30 seconds
    @Scheduled(fixedDelay = 30000)
    public void simulateOrderProgression() {
        List<Order> activeOrders = orderTrackingService.getActiveOrders();

        for (Order order : activeOrders) {
            // Randomly progress orders through statuses
            if (shouldProgressOrder()) {
                progressOrderStatus(order);
            }

            // Simulate location updates for orders out for delivery
            if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
                simulateLocationUpdate(order);
            }
        }
    }

    private boolean shouldProgressOrder() {
        return random.nextDouble() < 0.3; // 30% chance to progress
    }

    private void progressOrderStatus(Order order) {
        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus = getNextStatus(currentStatus);

        if (nextStatus != null && nextStatus != currentStatus) {
            String notes = generateStatusNotes(nextStatus);
            orderTrackingService.updateOrderStatus(order.getOrderNumber(), nextStatus, notes);

            // Assign driver when order is ready for pickup
            if (nextStatus == OrderStatus.READY_FOR_PICKUP) {
                assignRandomDriver(order);
            }
        }
    }

    private OrderStatus getNextStatus(OrderStatus currentStatus) {
        return switch (currentStatus) {
            case PLACED -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> OrderStatus.PICKED_UP;
            case PICKED_UP -> OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> OrderStatus.DELIVERED;
            default -> null;
        };
    }

    private String generateStatusNotes(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Restaurant confirmed your order";
            case PREPARING -> "Chef is preparing your delicious meal";
            case READY_FOR_PICKUP -> "Order is ready and waiting for driver";
            case PICKED_UP -> "Driver has picked up your order";
            case OUT_FOR_DELIVERY -> "On the way to your location";
            case DELIVERED -> "Order delivered successfully!";
            default -> "Status updated";
        };
    }

    private void assignRandomDriver(Order order) {
        String[] driverIds = {"DRIVER001", "DRIVER002", "DRIVER003", "DRIVER004", "DRIVER005"};
        String randomDriver = driverIds[random.nextInt(driverIds.length)];

        // Actually assign the driver to the order
        order.setDeliveryDriverId(randomDriver);
        orderRepository.save(order);
    }

    private void simulateLocationUpdate(Order order) {
        if (order.getDeliveryAddress() != null &&
                order.getDeliveryAddress().getLatitude() != null &&
                order.getDeliveryAddress().getLongitude() != null) {

            // Simulate movement towards the delivery address
            double baseLat = order.getDeliveryAddress().getLatitude();
            double baseLng = order.getDeliveryAddress().getLongitude();

            // Add some random movement (simulating driver location)
            double currentLat = baseLat + (random.nextDouble() - 0.5) * 0.01;
            double currentLng = baseLng + (random.nextDouble() - 0.5) * 0.01;

            String driverId = order.getDeliveryDriverId() != null ?
                    order.getDeliveryDriverId() : "DRIVER001";

            orderTrackingService.updateDeliveryLocation(
                    order.getOrderNumber(), currentLat, currentLng, driverId);
        }
    }
}