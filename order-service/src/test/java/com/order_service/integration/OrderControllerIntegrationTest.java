package com.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service.dto.request.OrderItemRequestDTO;
import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.service.RestaurantValidationService;
import com.order_service.util.TestJwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantValidationService restaurantValidationService;

    @Autowired
    private TestJwtUtil testJwtUtil;

    private OrderRequestDTO validOrderRequest;

    @BeforeEach
    void setup() {
        validOrderRequest = OrderRequestDTO.builder()
                .restaurantId(1L)
                .restaurantName("The Grill House")
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .menuItemId(100L)
                                .menuItemName("Burger")
                                .quantity(2)
                                .unitPrice(new BigDecimal("5.00"))
                                .build()
                ))
                .build();

        // Mock validation to do nothing
        doNothing().when(restaurantValidationService).validateOrderRequest(validOrderRequest);
    }

    @Test
    void createOrder_shouldReturnCreatedOrder_withValidJWT() throws Exception {
        String jwt = testJwtUtil.generateToken("user-abc", "user-abc@gmail.com", "ROLE_CUSTOMER");

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .header("X-User-Id", "user-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantName").value("The Grill House"))
                .andExpect(jsonPath("$.totalAmount").value("10.00"));
    }

    @Test
    void createOrder_shouldReturnUnauthorized_whenJWTIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", "user-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public RestaurantValidationService restaurantValidationService() {
            return Mockito.mock(RestaurantValidationService.class);
        }
    }
}
