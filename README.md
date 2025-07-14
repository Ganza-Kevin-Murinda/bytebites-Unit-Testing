# 🧪 Order Service – Lab Work 7: Testing, Observability & Code Review for Microservices

## 📌 Overview

This repository contains the enhanced `order-service` microservice for **Lab Work 7**, focusing on improving the **testing**, **observability**, and **code quality** of a Spring Boot-based microservice. It builds upon the foundations laid in Week 6.

---

## 🔧 Technologies Used

- **Spring Boot**
- **Spring Security (JWT)**
- **JUnit 5 + Mockito**
- **TestContainers**
- **SLF4J (Structured Logging)**
- **PostgreSQL**
- **MapStruct (DTO Mapping)**
- **MockMvc (Integration Testing)**

---

## ✅ Features Implemented

### 🔐 Input Validation

- Validation on `OrderRequestDTO` and nested `OrderItemRequestDTO`
- Uses annotations like `@NotNull`, `@NotEmpty`, `@Valid`
- Nested object validation enabled via `@Valid` on collections

---

### ⚠️ Centralized Exception Handling

- Global exception handler using `@RestControllerAdvice`
- Consistent JSON error responses for:
  - Validation errors
  - Domain-specific errors (e.g., `OrderNotFoundException`)
  - Access-denied errors

---

### 📘 Logging and Observability

- Structured logging via **SLF4J**
- All critical actions logged with metadata:
  - `customerId`, `orderId`, `restaurantId`
- Consistent logging across:
  - Controller layer (`@Slf4j`)
  - Service layer
  - Exception handler

---

### 🧪 Unit Testing with JUnit + Mockito

- Added **unit tests** for service methods:
  - Mocked repository and validation layers
  - Success and error paths verified
- Used **Mockito annotations** and best practices:
  - `@Mock`, `@InjectMocks`, `@BeforeEach`, etc.

---

### 🌐 Integration Testing with TestContainers

- Bootstrapped PostgreSQL container using TestContainers
- Used **`@SpringBootTest` + `MockMvc`** to hit real API endpoints
- Verified full request/response cycle including:
  - JWT security enforcement
  - Custom headers (`X-User-Id`)
  - Validation and persistence logic
- Created isolated and reproducible test data

---

## 🧪 Example Integration Test

```java
@Test
void createOrder_shouldReturnCreatedOrder_withValidJWT() throws Exception {
    String jwt = testJwtUtil.generateToken("user-abc", "user-abc@gmail.com", "ROLE_CUSTOMER");

    mockMvc.perform(post("/api/orders")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .header("X-User-Id", "user-abc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.restaurantName").value("The Grill House"))
        .andExpect(jsonPath("$.totalAmount").value("10.00"));
}
```

## 📁 Test Profile Configuration
- A test profile was used via `@ActiveProfiles("test")`
- Application config: `application-test.yml` uses TestContainers (PostgreSQL)
- Security config supports test-generated JWTs

---

## 🤝 Code Review Note
```bash
Link
```
---

### 👨‍💻 Author
> Ganza Kevin Murinda
