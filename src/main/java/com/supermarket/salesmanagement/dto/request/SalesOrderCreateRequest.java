package com.supermarket.salesmanagement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class SalesOrderCreateRequest {
    @NotNull(message = "Customer ID is mandatory")
    private UUID customerId;

    @NotNull(message = "Shop ID is mandatory")
    private UUID shopId;

    @NotNull(message = "Order date is mandatory")
    private LocalDate orderDate;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is mandatory")
        private UUID productId;

        @NotNull(message = "Quantity is mandatory")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull(message = "Unit price is mandatory")
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;
    }
}