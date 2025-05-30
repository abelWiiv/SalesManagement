package com.supermarket.salesmanagement.service;

import com.supermarket.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderItemAddRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderUpdateRequest;
import com.supermarket.salesmanagement.dto.response.SalesOrderResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderItemRepository salesOrderItemRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private ShopClient shopClient;

    @Mock
    private OrderStatusPublisher orderStatusPublisher;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private UUID orderId;
    private UUID customerId;
    private UUID shopId;
    private UUID productId;
    private SalesOrder salesOrder;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        shopId = UUID.randomUUID();
        productId = UUID.randomUUID();

        salesOrder = SalesOrder.builder()
                .id(orderId)
                .customerId(customerId)
                .shopId(shopId)
                .orderDate(LocalDate.now())
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createSalesOrder_Success() {
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setCustomerId(customerId);
        request.setShopId(shopId);
        request.setOrderDate(LocalDate.now());

        SalesOrderCreateRequest.OrderItemRequest item = new SalesOrderCreateRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        request.setItems(List.of(item));

        when(customerClient.getCustomerById(customerId)).thenReturn(null);
        when(shopClient.getShopById(shopId)).thenReturn(null);
        when(productClient.getProductById(productId)).thenReturn(null);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.createSalesOrder(request);

        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(shopId, response.getShopId());
        assertEquals(OrderStatus.DRAFT, response.getStatus());
        verify(salesOrderRepository).save(any(SalesOrder.class));
    }

    @Test
    void createSalesOrder_NullCustomerId_ThrowsException() {
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setShopId(shopId);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.createSalesOrder(request));
        assertEquals("Customer ID is required", exception.getMessage());
    }

    @Test
    void createSalesOrder_NullShopId_ThrowsException() {
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setCustomerId(customerId);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.createSalesOrder(request));
        assertEquals("Shop ID is required", exception.getMessage());
    }

    @Test
    void createSalesOrder_InvalidItemData_ThrowsException() {
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setCustomerId(customerId);
        request.setShopId(shopId);

        SalesOrderCreateRequest.OrderItemRequest item = new SalesOrderCreateRequest.OrderItemRequest();
        item.setProductId(null);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        request.setItems(List.of(item));

//        when(customerClient.getCustomerById(customerId)).thenReturn(null);
//        when(shopClient.getShopById(shopId)).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.createSalesOrder(request));
        assertEquals("Invalid item data: product ID, quantity, and unit price are required", exception.getMessage());
    }

    @Test
    void createSalesOrder_InvalidQuantity_ThrowsException() {
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setCustomerId(customerId);
        request.setShopId(shopId);

        SalesOrderCreateRequest.OrderItemRequest item = new SalesOrderCreateRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(0);
        item.setUnitPrice(new BigDecimal("10.00"));
        request.setItems(List.of(item));

        when(customerClient.getCustomerById(customerId)).thenReturn(null);
        when(shopClient.getShopById(shopId)).thenReturn(null);
        when(productClient.getProductById(productId)).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.createSalesOrder(request));
        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void updateSalesOrder_Success() {
        SalesOrderUpdateRequest request = new SalesOrderUpdateRequest();
        request.setCustomerId(customerId);
        request.setStatus(OrderStatus.PENDING);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(customerClient.getCustomerById(customerId)).thenReturn(null);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.updateSalesOrder(orderId, request);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(salesOrderRepository).save(any(SalesOrder.class));
        verify(orderStatusPublisher).publishOrderStatusEvent(any());
    }

    @Test
    void updateSalesOrder_NotFound_ThrowsException() {
        SalesOrderUpdateRequest request = new SalesOrderUpdateRequest();
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.updateSalesOrder(orderId, request));
        assertEquals("Sales order with ID " + orderId + " not found", exception.getMessage());
    }

    @Test
    void updateSalesOrder_InvalidStatusTransition_ThrowsException() {
        salesOrder.setStatus(OrderStatus.CONFIRMED);
        SalesOrderUpdateRequest request = new SalesOrderUpdateRequest();
        request.setStatus(OrderStatus.PENDING);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.updateSalesOrder(orderId, request));
        assertEquals("Confirmed orders can only be transitioned to CANCELLED", exception.getMessage());
    }

    @Test
    void updateSalesOrder_CancelledOrder_ThrowsException() {
        salesOrder.setStatus(OrderStatus.CANCELLED);
        SalesOrderUpdateRequest request = new SalesOrderUpdateRequest();
        request.setStatus(OrderStatus.PENDING);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.updateSalesOrder(orderId, request));
        assertEquals("Cancelled orders cannot be modified", exception.getMessage());
    }

    @Test
    void addSalesOrderItem_Success() {
        SalesOrderItemAddRequest request = new SalesOrderItemAddRequest();
        request.setProductId(productId);
        request.setQuantity(1);
        request.setUnitPrice(new BigDecimal("15.00"));

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(productClient.getProductById(productId)).thenReturn(null);
        when(salesOrderItemRepository.save(any(SalesOrderItem.class))).thenReturn(new SalesOrderItem());
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.addSalesOrderItem(orderId, request);

        assertNotNull(response);
        verify(salesOrderItemRepository).save(any(SalesOrderItem.class));
        verify(salesOrderRepository).save(any(SalesOrder.class));
        verify(orderStatusPublisher).publishOrderStatusEvent(any());
    }

    @Test
    void addSalesOrderItem_ConfirmedOrder_ThrowsException() {
        salesOrder.setStatus(OrderStatus.CONFIRMED);
        SalesOrderItemAddRequest request = new SalesOrderItemAddRequest();

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.addSalesOrderItem(orderId, request));
        assertEquals("Cannot add items to a confirmed order", exception.getMessage());
    }

    @Test
    void addSalesOrderItem_PendingOrder_ThrowsException() {
        salesOrder.setStatus(OrderStatus.PENDING);
        SalesOrderItemAddRequest request = new SalesOrderItemAddRequest();

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.addSalesOrderItem(orderId, request));
        assertEquals("Cannot add items to a pending order", exception.getMessage());
    }

    @Test
    void addSalesOrderItem_NegativePrice_ThrowsException() {
        SalesOrderItemAddRequest request = new SalesOrderItemAddRequest();
        request.setProductId(productId);
        request.setQuantity(1);
        request.setUnitPrice(new BigDecimal("-15.00"));

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(productClient.getProductById(productId)).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.addSalesOrderItem(orderId, request));
        assertEquals("Unit price must be greater than zero", exception.getMessage());
    }

    @Test
    void deleteSalesOrderItem_Success() {
        SalesOrderItem item = SalesOrderItem.builder()
                .id(UUID.randomUUID())
                .salesOrder(salesOrder)
                .productId(productId)
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build();
        salesOrder.getItems().add(item);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.deleteSalesOrderItem(orderId, item.getId());

        assertNotNull(response);
        verify(salesOrderItemRepository).delete(any(SalesOrderItem.class));
        verify(salesOrderRepository).save(any(SalesOrder.class));
    }

    @Test
    void deleteSalesOrderItem_ItemNotFound_ThrowsException() {
        UUID itemId = UUID.randomUUID();
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.deleteSalesOrderItem(orderId, itemId));
        assertEquals("Sales order item with ID " + itemId + " not found in order " + orderId, exception.getMessage());
    }

    @Test
    void deleteSalesOrderItem_CancelledOrder_ThrowsException() {
        salesOrder.setStatus(OrderStatus.CANCELLED);
        UUID itemId = UUID.randomUUID();

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.deleteSalesOrderItem(orderId, itemId));
        assertEquals("Cannot delete items from a cancelled order", exception.getMessage());
    }

    @Test
    void confirmOrderAfterPayment_Success() {
        Invoice invoice = new Invoice();
        invoice.setPaymentStatus(PaymentStatus.PAID);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.findBySalesOrderId(orderId)).thenReturn(Optional.of(invoice));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        SalesOrderResponse response = salesOrderService.confirmOrderAfterPayment(orderId);

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(orderStatusPublisher).publishOrderStatusEvent(any());
    }

    @Test
    void confirmOrderAfterPayment_UnpaidInvoice_ThrowsException() {
        Invoice invoice = new Invoice();
        invoice.setPaymentStatus(PaymentStatus.UNPAID);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.findBySalesOrderId(orderId)).thenReturn(Optional.of(invoice));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.confirmOrderAfterPayment(orderId));
        assertEquals("Invoice for sales order " + orderId + " is not fully paid. Current status: UNPAID", exception.getMessage());
    }

    @Test
    void confirmOrderAfterPayment_NoInvoice_ThrowsException() {
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.findBySalesOrderId(orderId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.confirmOrderAfterPayment(orderId));
        assertEquals("No invoice found for sales order " + orderId, exception.getMessage());
    }

    @Test
    void confirmOrderAfterPayment_AlreadyConfirmed_ThrowsException() {
        salesOrder.setStatus(OrderStatus.CONFIRMED);
        Invoice invoice = new Invoice();
        invoice.setPaymentStatus(PaymentStatus.PAID);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.findBySalesOrderId(orderId)).thenReturn(Optional.of(invoice));

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.confirmOrderAfterPayment(orderId));
        assertEquals("Sales order " + orderId + " is already confirmed", exception.getMessage());
    }

    @Test
    void getSalesOrderById_Success() {
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        SalesOrderResponse response = salesOrderService.getSalesOrderById(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getId());
    }

    @Test
    void getAllSalesOrders_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SalesOrder> page = new PageImpl<>(List.of(salesOrder));
        when(salesOrderRepository.findAll(pageable)).thenReturn(page);

        Page<SalesOrderResponse> result = salesOrderService.getAllSalesOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void deleteSalesOrder_Success() {
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.existsBySalesOrderId(orderId)).thenReturn(false);

        salesOrderService.deleteSalesOrder(orderId);

        verify(salesOrderRepository).delete(salesOrder);
    }

    @Test
    void deleteSalesOrder_WithInvoice_ThrowsException() {
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.existsBySalesOrderId(orderId)).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.deleteSalesOrder(orderId));
        assertEquals("Cannot delete sales order with associated invoices", exception.getMessage());
    }

    @Test
    void deleteSalesOrder_NonDraftOrder_ThrowsException() {
        salesOrder.setStatus(OrderStatus.PENDING);
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.existsBySalesOrderId(orderId)).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class,
                () -> salesOrderService.deleteSalesOrder(orderId));
        assertEquals("Only DRAFT orders can be deleted", exception.getMessage());
    }
}