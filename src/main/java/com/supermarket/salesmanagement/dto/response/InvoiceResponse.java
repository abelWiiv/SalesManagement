package com.supermarket.salesmanagement.dto.response;

import com.supermarket.salesmanagement.model.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InvoiceResponse {
    private UUID id;
    private UUID salesOrderId;
    private LocalDate invoiceDate;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}