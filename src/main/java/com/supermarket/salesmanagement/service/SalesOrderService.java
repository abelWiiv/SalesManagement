package com.supermarket.salesmanagement.service;

import com.supermarket.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderItemAddRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderUpdateRequest;
import com.supermarket.salesmanagement.dto.response.SalesOrderResponse;
import com.supermarket.salesmanagement.event.OrderStatusEvent;
import com.supermarket.salesmanagement.event.OrderStatusPublisher;
import com.supermarket.salesmanagement.exception.CustomException;
import com.supermarket.salesmanagement.model.Invoice;
import com.supermarket.salesmanagement.model.SalesOrder;
import com.supermarket.salesmanagement.model.SalesOrderItem;
import com.supermarket.salesmanagement.model.enums.OrderStatus;
import com.supermarket.salesmanagement.model.enums.PaymentStatus;
import com.supermarket.salesmanagement.repository.InvoiceRepository;
import com.supermarket.salesmanagement.repository.SalesOrderItemRepository;
import com.supermarket.salesmanagement.repository.SalesOrderRepository;
import com.supermarket.salesmanagement.service.client.CustomerClient;
import com.supermarket.salesmanagement.service.client.ProductClient;
import com.supermarket.salesmanagement.service.client.ShopClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final ShopClient shopClient;
    private final OrderStatusPublisher orderStatusPublisher;

    @Transactional
    public SalesOrderResponse createSalesOrder(SalesOrderCreateRequest request) {
        validateCreateRequest(request);

        // Validate customer and shop
        customerClient.getCustomerById(request.getCustomerId());
        shopClient.getShopById(request.getShopId());

        SalesOrder salesOrder = SalesOrder.builder()
                .customerId(request.getCustomerId())
                .shopId(request.getShopId())
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now())
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        // Process items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<SalesOrderItem> items = request.getItems().stream()
                    .peek(item -> {
                        productClient.getProductById(item.getProductId());
                        validateItem(item.getQuantity(), item.getUnitPrice());
                    })
                    .map(item -> SalesOrderItem.builder()
                            .salesOrder(salesOrder)
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build())
                    .collect(Collectors.toList());
            salesOrder.setItems(items);
            salesOrder.calculateTotalAmount();
        }

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
//        orderStatusPublisher.publishOrderStatusEvent(new OrderStatusEvent(savedOrder.getId(), savedOrder.getStatus()));
        return mapToSalesOrderResponse(savedOrder);
    }

    @Transactional
    public SalesOrderResponse updateSalesOrder(UUID id, SalesOrderUpdateRequest request) {
        validateUpdateRequest(request);

        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new CustomException("Sales order with ID " + id + " not found"));

        // Validate and update fields
        if (request.getCustomerId() != null) {
            customerClient.getCustomerById(request.getCustomerId());
            salesOrder.setCustomerId(request.getCustomerId());
        }
        if (request.getShopId() != null) {
            shopClient.getShopById(request.getShopId());
            salesOrder.setShopId(request.getShopId());
        }
        if (request.getOrderDate() != null) {
            salesOrder.setOrderDate(request.getOrderDate());
        }
        if (request.getStatus() != null) {
            validateStatusTransition(salesOrder.getStatus(), request.getStatus());
            salesOrder.setStatus(request.getStatus());
        }

        // Update items if provided
        if (request.getItems() != null) {
            salesOrderItemRepository.deleteBySalesOrderId(salesOrder.getId());
            salesOrder.getItems().clear();

            if (!request.getItems().isEmpty()) {
                List<SalesOrderItem> newItems = request.getItems().stream()
                        .peek(item -> {
                            productClient.getProductById(item.getProductId());
                            validateItem(item.getQuantity(), item.getUnitPrice());
                        })
                        .map(item -> SalesOrderItem.builder()
                                .salesOrder(salesOrder)
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .build())
                        .collect(Collectors.toList());
                salesOrder.getItems().addAll(newItems);
            }
            salesOrder.calculateTotalAmount();
        }

        SalesOrder updatedOrder = salesOrderRepository.save(salesOrder);
        orderStatusPublisher.publishOrderStatusEvent(new OrderStatusEvent(updatedOrder.getId(), updatedOrder.getStatus()));
        return mapToSalesOrderResponse(updatedOrder);
    }

    @Transactional
    public SalesOrderResponse addSalesOrderItem(UUID orderId, SalesOrderItemAddRequest request) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Sales order with ID " + orderId + " not found"));

        // Prevent adding items to confirmed or cancelled orders
        if (salesOrder.getStatus() == OrderStatus.PENDING) {
            throw new CustomException("Cannot add items to a pending order");
        }
        if (salesOrder.getStatus() == OrderStatus.CONFIRMED) {
            throw new CustomException("Cannot add items to a confirmed order");
        }
        if (salesOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new CustomException("Cannot add items to a cancelled order");
        }

        // Validate item data
        productClient.getProductById(request.getProductId());
        validateItem(request.getQuantity(), request.getUnitPrice());

        // Create new sales order item
        SalesOrderItem newItem = SalesOrderItem.builder()
                .salesOrder(salesOrder)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                .build();

        // Add item to sales order and save it
        salesOrder.getItems().add(newItem);
        salesOrder.calculateTotalAmount(); // Recalculate total amount
        salesOrderItemRepository.save(newItem); // Save the new item
        SalesOrder updatedOrder = salesOrderRepository.save(salesOrder); // Save the updated sales order

        orderStatusPublisher.publishOrderStatusEvent(new OrderStatusEvent(updatedOrder.getId(), updatedOrder.getStatus()));
        return mapToSalesOrderResponse(updatedOrder);
    }

    @Transactional
    public SalesOrderResponse deleteSalesOrderItem(UUID orderId, UUID itemId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Sales order with ID " + orderId + " not found"));

        // Prevent deleting items from confirmed or cancelled orders
        if (salesOrder.getStatus() == OrderStatus.PENDING) {
            throw new CustomException("Cannot delete items from a pending order");
        }
        if (salesOrder.getStatus() == OrderStatus.CONFIRMED) {
            throw new CustomException("Cannot delete items from a confirmed order");
        }
        if (salesOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new CustomException("Cannot delete items from a cancelled order");
        }

        // Find and remove the item
        SalesOrderItem itemToRemove = salesOrder.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CustomException("Sales order item with ID " + itemId + " not found in order " + orderId));

        salesOrder.getItems().remove(itemToRemove);
        salesOrderItemRepository.delete(itemToRemove);
        salesOrder.calculateTotalAmount();

        SalesOrder updatedOrder = salesOrderRepository.save(salesOrder);
        orderStatusPublisher.publishOrderStatusEvent(new OrderStatusEvent(updatedOrder.getId(), updatedOrder.getStatus()));
        return mapToSalesOrderResponse(updatedOrder);
    }



    @Transactional
    public SalesOrderResponse confirmOrderAfterPayment(UUID orderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Sales order with ID " + orderId + " not found"));

        Invoice invoice = invoiceRepository.findBySalesOrderId(orderId)
                .orElseThrow(() -> new CustomException("No invoice found for sales order " + orderId));

        if (invoice.getPaymentStatus() != PaymentStatus.PAID) {
            throw new CustomException("Invoice for sales order " + orderId + " is not fully paid. Current status: " + invoice.getPaymentStatus());
        }

        if (salesOrder.getStatus() == OrderStatus.CONFIRMED) {
            throw new CustomException("Sales order " + orderId + " is already confirmed");
        }

        salesOrder.setStatus(OrderStatus.CONFIRMED);
        salesOrder.calculateTotalAmount();
        SalesOrder confirmedOrder = salesOrderRepository.save(salesOrder);

        orderStatusPublisher.publishOrderStatusEvent(new OrderStatusEvent(confirmedOrder.getId(), confirmedOrder.getStatus()));
        return mapToSalesOrderResponse(confirmedOrder);
    }

    public SalesOrderResponse getSalesOrderById(UUID id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new CustomException("Sales order with ID " + id + " not found"));
        return mapToSalesOrderResponse(salesOrder);
    }

    public Page<SalesOrderResponse> getAllSalesOrders(Pageable pageable) {
        return salesOrderRepository.findAll(pageable)
                .map(this::mapToSalesOrderResponse);
    }

    @Transactional
    public void deleteSalesOrder(UUID id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new CustomException("Sales order with ID " + id + " not found"));

        if (invoiceRepository.existsBySalesOrderId(id)) {
            throw new CustomException("Cannot delete sales order with associated invoices");
        }
        if (salesOrder.getStatus() != OrderStatus.DRAFT) {
            throw new CustomException("Only DRAFT orders can be deleted");
        }
        salesOrderRepository.delete(salesOrder);
    }

    private void validateCreateRequest(SalesOrderCreateRequest request) {
        if (request.getCustomerId() == null) {
            throw new CustomException("Customer ID is required");
        }
        if (request.getShopId() == null) {
            throw new CustomException("Shop ID is required");
        }
        if (request.getItems() != null) {
            request.getItems().forEach(item -> {
                if (item.getProductId() == null || item.getQuantity() == null || item.getUnitPrice() == null) {
                    throw new CustomException("Invalid item data: product ID, quantity, and unit price are required");
                }
            });
        }
    }

    private void validateUpdateRequest(SalesOrderUpdateRequest request) {
        if (request.getItems() != null) {
            request.getItems().forEach(item -> {
                if (item.getProductId() == null || item.getQuantity() == null || item.getUnitPrice() == null) {
                    throw new CustomException("Invalid item data: product ID, quantity, and unit price are required");
                }
            });
        }
    }

    private void validateItem(Integer quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new CustomException("Quantity must be greater than zero");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Unit price must be greater than zero");
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
            throw new CustomException("Confirmed orders can only be transitioned to CANCELLED");
        }
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new CustomException("Cancelled orders cannot be modified");
        }
    }

    private SalesOrderResponse mapToSalesOrderResponse(SalesOrder salesOrder) {
        SalesOrderResponse response = new SalesOrderResponse();
        response.setId(salesOrder.getId());
        response.setCustomerId(salesOrder.getCustomerId());
        response.setShopId(salesOrder.getShopId());
        response.setOrderDate(salesOrder.getOrderDate());
        response.setStatus(salesOrder.getStatus());
        response.setCreatedAt(salesOrder.getCreatedAt());
        response.setUpdatedAt(salesOrder.getUpdatedAt());
        response.setTotalAmount(salesOrder.getTotalAmount());
        response.setItems(salesOrder.getItems().stream()
                .map(item -> {
                    SalesOrderResponse.OrderItemResponse itemResponse = new SalesOrderResponse.OrderItemResponse();
                    itemResponse.setId(item.getId());
                    itemResponse.setProductId(item.getProductId());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());
                    itemResponse.setTotalPrice(item.getTotalPrice());
                    itemResponse.setCreatedAt(item.getCreatedAt());
                    itemResponse.setUpdatedAt(item.getUpdatedAt());
                    return itemResponse;
                })
                .collect(Collectors.toList()));
        return response;
    }
}

