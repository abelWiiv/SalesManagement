package com.supermarket.salesmanagement.service;

import com.supermarket.salesmanagement.dto.request.InvoiceCreateRequest;
import com.supermarket.salesmanagement.dto.request.InvoiceUpdateRequest;
import com.supermarket.salesmanagement.dto.response.InvoiceResponse;
import com.supermarket.salesmanagement.exception.CustomException;
import com.supermarket.salesmanagement.model.Invoice;
import com.supermarket.salesmanagement.model.enums.PaymentStatus;
import com.supermarket.salesmanagement.repository.InvoiceRepository;
import com.supermarket.salesmanagement.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final SalesOrderRepository salesOrderRepository;

    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        if (!salesOrderRepository.existsById(request.getSalesOrderId())) {
            throw new CustomException("Sales order with ID " + request.getSalesOrderId() + " not found");
        }
        if (invoiceRepository.existsBySalesOrderId(request.getSalesOrderId())) {
            throw new CustomException("Invoice for sales order ID " + request.getSalesOrderId() + " already exists");
        }

        Invoice invoice = Invoice.builder()
                .salesOrderId(request.getSalesOrderId())
                .invoiceDate(request.getInvoiceDate())
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(savedInvoice);
    }

    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new CustomException("Invoice with ID " + id + " not found"));
        return mapToInvoiceResponse(invoice);
    }

    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable)
                .map(this::mapToInvoiceResponse);
    }

    public InvoiceResponse updateInvoice(UUID id, InvoiceUpdateRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new CustomException("Invoice with ID " + id + " not found"));

        if (request.getSalesOrderId() != null) {
            if (!salesOrderRepository.existsById(request.getSalesOrderId())) {
                throw new CustomException("Sales order with ID " + request.getSalesOrderId() + " not found");
            }
            invoice.setSalesOrderId(request.getSalesOrderId());
        }
        if (request.getInvoiceDate() != null) {
            invoice.setInvoiceDate(request.getInvoiceDate());
        }
        if (request.getPaymentStatus() != null) {
            invoice.setPaymentStatus(request.getPaymentStatus());
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(updatedInvoice);
    }

    public void deleteInvoice(UUID id) {
        if (!invoiceRepository.existsById(id)) {
            throw new CustomException("Invoice with ID " + id + " not found");
        }
        invoiceRepository.deleteById(id);
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setSalesOrderId(invoice.getSalesOrderId());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setPaymentStatus(invoice.getPaymentStatus());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());
        return response;
    }
}