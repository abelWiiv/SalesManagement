package com.supermarket.salesmanagement.dto.request;

import com.supermarket.salesmanagement.model.enums.OrderStatus;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class SalesOrderUpdateRequest {
    private UUID customerId;
    private UUID shopId;
    private LocalDate orderDate;
    private OrderStatus status;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private UUID productId;
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;
    }
}