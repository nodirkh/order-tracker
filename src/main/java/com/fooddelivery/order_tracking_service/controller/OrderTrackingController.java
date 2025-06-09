package com.fooddelivery.order_tracking_service.controller;

import com.fooddelivery.order_tracking_service.dto.CreateOrderRequest;
import com.fooddelivery.order_tracking_service.dto.OrderTrackingResponse;
import com.fooddelivery.order_tracking_service.model.Order;
import com.fooddelivery.order_tracking_service.model.OrderStatus;
import com.fooddelivery.order_tracking_service.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Tracking", description = "Order tracking and management APIs")
public class OrderTrackingController {

    @Autowired
    private OrderTrackingService orderTrackingService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new food delivery order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderTrackingService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{orderNumber}/track")
    @Operation(summary = "Get order tracking information", description = "Retrieves tracking details for a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order tracking information retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderTrackingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderTrackingResponse> trackOrder(
            @Parameter(description = "Order number", required = true) @PathVariable String orderNumber) {
        return orderTrackingService.getOrderTracking(orderNumber)
                .map(tracking -> ResponseEntity.ok(tracking))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{orderNumber}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Order> updateOrderStatus(
            @Parameter(description = "Order number", required = true) @PathVariable String orderNumber,
            @RequestBody Map<String, Object> statusUpdate) {

        OrderStatus newStatus = OrderStatus.valueOf((String) statusUpdate.get("status"));
        String notes = (String) statusUpdate.getOrDefault("notes", "");

        try {
            Order updatedOrder = orderTrackingService.updateOrderStatus(orderNumber, newStatus, notes);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{orderNumber}/location")
    @Operation(summary = "Update delivery location", description = "Updates the current location of a delivery driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> updateLocation(
            @Parameter(description = "Order number", required = true) @PathVariable String orderNumber,
            @RequestBody Map<String, Object> locationData) {

        Double latitude = Double.valueOf(locationData.get("latitude").toString());
        Double longitude = Double.valueOf(locationData.get("longitude").toString());
        String driverId = (String) locationData.get("driverId");

        orderTrackingService.updateDeliveryLocation(orderNumber, latitude, longitude, driverId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer", description = "Retrieves all orders for a specific customer")
    @ApiResponse(responseCode = "200", description = "Customer orders retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    public ResponseEntity<List<Order>> getCustomerOrders(
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId) {
        List<Order> orders = orderTrackingService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active orders", description = "Retrieves all orders that are currently active")
    @ApiResponse(responseCode = "200", description = "Active orders retrieved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    public ResponseEntity<List<Order>> getActiveOrders() {
        List<Order> activeOrders = orderTrackingService.getActiveOrders();
        return ResponseEntity.ok(activeOrders);
    }
}