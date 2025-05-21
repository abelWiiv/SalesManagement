package com.supermarket.salesmanagement.dto.response;

import com.supermarket.salesmanagement.model.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SalesOrderResponse {
    private UUID id;
    private UUID customerId;
    private UUID shopId;
    private LocalDate orderDate;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalAmount; // Added field
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private UUID id;
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}