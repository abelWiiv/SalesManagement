package com.supermarket.salesmanagement.service;

import com.supermarket.salesmanagement.dto.request.InvoiceCreateRequest;
import com.supermarket.salesmanagement.dto.request.InvoiceUpdateRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderUpdateRequest;
import com.supermarket.salesmanagement.dto.response.InvoiceResponse;
import com.supermarket.salesmanagement.exception.CustomException;
import com.supermarket.salesmanagement.model.Invoice;
import com.supermarket.salesmanagement.model.SalesOrder;
import com.supermarket.salesmanagement.model.enums.OrderStatus;
import com.supermarket.salesmanagement.model.enums.PaymentStatus;
import com.supermarket.salesmanagement.repository.InvoiceRepository;
import com.supermarket.salesmanagement.repository.SalesOrderRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderService salesOrderService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice invoice;
    private SalesOrder salesOrder;
    private UUID invoiceId;
    private UUID salesOrderId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        salesOrderId = UUID.randomUUID();
        now = LocalDateTime.now();

        salesOrder = SalesOrder.builder()
                .id(salesOrderId)
                .status(OrderStatus.PENDING)
                .build();

        invoice = Invoice.builder()
                .id(invoiceId)
                .salesOrderId(salesOrderId)
                .invoiceDate(LocalDate.now())
                .paymentStatus(PaymentStatus.UNPAID)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void createInvoice_Success() {
        // Arrange
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());

        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.existsBySalesOrderId(salesOrderId)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // Act
        InvoiceResponse response = invoiceService.createInvoice(request);

        // Assert
        assertNotNull(response);
        assertEquals(invoiceId, response.getId());
        assertEquals(salesOrderId, response.getSalesOrderId());
        assertEquals(now, response.getInvoiceDate());
        assertEquals(PaymentStatus.UNPAID, response.getPaymentStatus());
        verify(salesOrderService, times(1)).updateSalesOrder(eq(salesOrderId), any(SalesOrderUpdateRequest.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void createInvoice_SalesOrderNotFound_ThrowsException() {
        // Arrange
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());

        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(request));
        assertEquals("Sales order with ID " + salesOrderId + " not found", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_SalesOrderCancelled_ThrowsException() {
        // Arrange
        salesOrder.setStatus(OrderStatus.CANCELLED);
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());

        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(request));
        assertEquals("Cannot create invoice for cancelled sales order with ID " + salesOrderId, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_InvoiceAlreadyExists_ThrowsException() {
        // Arrange
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());

        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.existsBySalesOrderId(salesOrderId)).thenReturn(true);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(request));
        assertEquals("Invoice for sales order ID " + salesOrderId + " already exists", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getInvoiceById_Success() {
        // Arrange
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // Act
        InvoiceResponse response = invoiceService.getInvoiceById(invoiceId);

        // Assert
        assertNotNull(response);
        assertEquals(invoiceId, response.getId());
        assertEquals(salesOrderId, response.getSalesOrderId());
        assertEquals(PaymentStatus.UNPAID, response.getPaymentStatus());
        verify(invoiceRepository, times(1)).findById(invoiceId);
    }

    @Test
    void getInvoiceById_NotFound_ThrowsException() {
        // Arrange
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.getInvoiceById(invoiceId));
        assertEquals("Invoice with ID " + invoiceId + " not found", exception.getMessage());
    }

    @Test
    void getAllInvoices_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Invoice> invoicePage = new PageImpl<>(Arrays.asList(invoice));
        when(invoiceRepository.findAll(pageable)).thenReturn(invoicePage);

        // Act
        Page<InvoiceResponse> result = invoiceService.getAllInvoices(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(invoiceId, result.getContent().get(0).getId());
        verify(invoiceRepository, times(1)).findAll(pageable);
    }

    @Test
    void updateInvoice_Success() {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now().plusDays(1));
        request.setPaymentStatus(PaymentStatus.PAID);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // Act
        InvoiceResponse response = invoiceService.updateInvoice(invoiceId, request);

        // Assert
        assertNotNull(response);
        assertEquals(invoiceId, response.getId());
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void deleteInvoice_Success() {
        // Arrange
        when(invoiceRepository.existsById(invoiceId)).thenReturn(true);

        // Act
        invoiceService.deleteInvoice(invoiceId);

        // Assert and show
        verify(invoiceRepository, times(1)).deleteById(invoiceId);
    }

    @Test
    void deleteInvoice_NotFound_ThrowsException() {
        // Arrange
        when(invoiceRepository.existsById(invoiceId)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.deleteInvoice(invoiceId));
        assertEquals("Invoice with ID " + invoiceId + " not found", exception.getMessage());
        verify(invoiceRepository, never()).deleteById(invoiceId);
    }

    @Test
    void createInvoice_NullRequest_ThrowsException() {
        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(null));
        assertEquals("Invoice creation request cannot be null", exception.getMessage());
        verify(salesOrderRepository, never()).findById(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_NullSalesOrderId_ThrowsException() {
        // Arrange
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(null);
        request.setInvoiceDate(LocalDate.now());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(request));
        assertEquals("Sales order ID cannot be null", exception.getMessage());
        verify(salesOrderRepository, never()).findById(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_NullInvoiceDate_ThrowsException() {
        // Arrange
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(null);

        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(invoiceRepository.existsBySalesOrderId(salesOrderId)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(request));
        assertEquals("Invoice date cannot be null", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_SalesOrderCompleted_ThrowsException() {
        // Arrange
        salesOrder.setStatus(OrderStatus.CONFIRMED);
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());

        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.createInvoice(request));
        assertEquals("Cannot create invoice for completed sales order with ID " + salesOrderId, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void updateInvoice_NullRequest_ThrowsException() {
        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.updateInvoice(invoiceId, null));
        assertEquals("Invoice update request cannot be null", exception.getMessage());
        verify(invoiceRepository, never()).findById(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void updateInvoice_NullSalesOrderId_ThrowsException() {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setSalesOrderId(null);
        request.setInvoiceDate(LocalDate.now());
        request.setPaymentStatus(PaymentStatus.PAID);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.updateInvoice(invoiceId, request));
        assertEquals("Sales order ID cannot be null", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void updateInvoice_SalesOrderNotFound_ThrowsException() {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());
        request.setPaymentStatus(PaymentStatus.PAID);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.updateInvoice(invoiceId, request));
        assertEquals("Sales order with ID " + salesOrderId + " not found", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void updateInvoice_PaymentStatusAlreadySet_ThrowsException() {
        // Arrange
        invoice.setPaymentStatus(PaymentStatus.PAID);
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setSalesOrderId(salesOrderId);
        request.setInvoiceDate(LocalDate.now());
        request.setPaymentStatus(PaymentStatus.PAID);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.updateInvoice(invoiceId, request));
        assertEquals("Payment status is already set to PAID for invoice with ID " + invoiceId, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getAllInvoices_EmptyPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Invoice> emptyPage = new PageImpl<>(Arrays.asList());
        when(invoiceRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<InvoiceResponse> result = invoiceService.getAllInvoices(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(invoiceRepository, times(1)).findAll(pageable);
    }

    @Test
    void deleteInvoice_NullId_ThrowsException() {
        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> invoiceService.deleteInvoice(null));
        assertEquals("Invoice ID cannot be null", exception.getMessage());
        verify(invoiceRepository, never()).deleteById(any());
    }

}
