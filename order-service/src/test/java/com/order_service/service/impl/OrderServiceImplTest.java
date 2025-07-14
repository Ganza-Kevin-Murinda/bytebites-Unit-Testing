package com.order_service.service.impl;

import com.order_service.dto.request.OrderItemRequestDTO;
import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.dto.response.OrderResponseDTO;
import com.order_service.exception.InvalidOrderException;
import com.order_service.mapper.OrderMapper;
import com.order_service.model.Order;
import com.order_service.model.OrderItem;
import com.order_service.model.OrderStatus;
import com.order_service.repository.OrderRepository;
import com.order_service.service.RestaurantValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantValidationService restaurantValidationService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequestDTO validOrderRequest;
    private Order validOrder;
    private Order savedOrder;
    private OrderResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        validOrderRequest = OrderRequestDTO.builder()
                .restaurantId(100L)
                .restaurantName("Test Restaurant")
                .items(List.of(OrderItemRequestDTO.builder()
                        .menuItemId(1L)
                        .menuItemName("Pizza")
                        .quantity(2)
                        .unitPrice(new BigDecimal("10.00"))
                        .build()))
                .build();

        validOrder = Order.builder()
                .restaurantId(100L)
                .restaurantName("Test Restaurant")
                .status(OrderStatus.PENDING)
                .build();

        savedOrder = Order.builder()
                .id(1L)
                .customerId("customer-123")
                .restaurantId(100L)
                .restaurantName("Test Restaurant")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("20.00"))
                .items(List.of(OrderItem.builder()
                        .menuItemId(1L)
                        .menuItemName("Pizza")
                        .quantity(2)
                        .unitPrice(new BigDecimal("10.00"))
                        .totalPrice(new BigDecimal("20.00"))
                        .build()))
                .build();

        responseDTO = OrderResponseDTO.builder()
                .id(1L)
                .customerId("customer-123")
                .restaurantId(100L)
                .restaurantName("Test Restaurant")
                .totalAmount(new BigDecimal("20.00"))
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    void createOrder_success_shouldReturnOrderResponseDTO() {
        when(orderMapper.toEntity(validOrderRequest)).thenReturn(validOrder);
        when(orderMapper.toEntityList(validOrderRequest.getItems()))
                .thenReturn(savedOrder.getItems());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.createOrder(validOrderRequest, "customer-123");

        assertNotNull(result);
        assertEquals("Test Restaurant", result.getRestaurantName());
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());

        verify(restaurantValidationService).validateOrderRequest(validOrderRequest);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_invalidQuantity_shouldThrowException() {
        validOrderRequest.getItems().getFirst().setQuantity(0);

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.createOrder(validOrderRequest, "customer-123")
        );

        assertEquals("Item quantity must be positive", ex.getMessage());
    }

    @Test
    void createOrder_emptyItems_shouldThrowException() {
        validOrderRequest.setItems(List.of());

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.createOrder(validOrderRequest, "customer-123")
        );

        assertEquals("Order must contain at least one item", ex.getMessage());
    }

}