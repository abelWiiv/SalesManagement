package com.supermarket.salesmanagement.event;

import com.supermarket.salesmanagement.model.enums.OrderStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderStatusEvent {
    private UUID orderId;
    private OrderStatus status;

    public OrderStatusEvent(UUID orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }
}