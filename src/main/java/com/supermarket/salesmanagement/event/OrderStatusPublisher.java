package com.supermarket.salesmanagement.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderStatusPublisher {
    public void publishOrderStatusEvent(OrderStatusEvent event) {
        // In a real implementation, this would publish to a message broker (e.g., RabbitMQ, Kafka)
        log.info("Order status event published: Order ID {}, Status {}", event.getOrderId(), event.getStatus());
    }
}