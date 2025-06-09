package com.fooddelivery.order_tracking_service.service;

import com.fooddelivery.order_tracking_service.dto.CreateOrderRequest;
import com.fooddelivery.order_tracking_service.dto.OrderTrackingResponse;
import com.fooddelivery.order_tracking_service.model.DeliveryAddress;
import com.fooddelivery.order_tracking_service.model.Order;
import com.fooddelivery.order_tracking_service.model.OrderStatus;
import com.fooddelivery.order_tracking_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderTrackingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OrderTrackingService orderTrackingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateOrder() {
        // Given
        DeliveryAddress address = new DeliveryAddress("123 Main St", "City", "12345", 40.7128, -74.0060);
        CreateOrderRequest request = new CreateOrderRequest("CUSTOMER001", "RESTAURANT001", address);

        Order mockOrder = new Order("ORD-123", "CUSTOMER001", "RESTAURANT001", address);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // When
        Order result = orderTrackingService.createOrder(request);

        // Then
        assertNotNull(result);
        assertEquals("CUSTOMER001", result.getCustomerId());
        assertEquals("RESTAURANT001", result.getRestaurantId());
        assertEquals(OrderStatus.PLACED, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetOrderTracking() {
        // Given
        String orderNumber = "ORD-123";
        DeliveryAddress address = new DeliveryAddress("123 Main St", "City", "12345", 40.7128, -74.0060);
        Order mockOrder = new Order(orderNumber, "CUSTOMER001", "RESTAURANT001", address);

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(mockOrder));

        // When
        Optional<OrderTrackingResponse> result = orderTrackingService.getOrderTracking(orderNumber);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderNumber, result.get().getOrderNumber());
        assertEquals(OrderStatus.PLACED, result.get().getCurrentStatus());
    }

    @Test
    void testUpdateOrderStatus() {
        // Given
        String orderNumber = "ORD-123";
        DeliveryAddress address = new DeliveryAddress("123 Main St", "City", "12345", 40.7128, -74.0060);
        Order mockOrder = new Order(orderNumber, "CUSTOMER001", "RESTAURANT001", address);

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // When
        Order result = orderTrackingService.updateOrderStatus(orderNumber, OrderStatus.CONFIRMED, "Order confirmed");

        // Then
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertFalse(result.getStatusHistory().isEmpty());
        verify(orderRepository).save(any(Order.class));
    }
}