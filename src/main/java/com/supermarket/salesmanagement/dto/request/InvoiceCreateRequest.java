package com.supermarket.salesmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class InvoiceCreateRequest {
    @NotNull(message = "Sales order ID is mandatory")
    private UUID salesOrderId;

    @NotNull(message = "Invoice date is mandatory")
    private LocalDate invoiceDate;
}