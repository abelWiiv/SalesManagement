package com.supermarket.salesmanagement.dto.request;

import com.supermarket.salesmanagement.model.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class InvoiceUpdateRequest {
    private UUID salesOrderId;
    private LocalDate invoiceDate;
    private PaymentStatus paymentStatus;
}