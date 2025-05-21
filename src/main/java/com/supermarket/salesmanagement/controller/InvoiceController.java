package com.supermarket.salesmanagement.controller;

import com.supermarket.salesmanagement.dto.request.InvoiceCreateRequest;
import com.supermarket.salesmanagement.dto.request.InvoiceUpdateRequest;
import com.supermarket.salesmanagement.dto.response.InvoiceResponse;
import com.supermarket.salesmanagement.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_INVOICE')")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_INVOICE')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_INVOICE')")
    public ResponseEntity<Page<InvoiceResponse>> getAllInvoices(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_INVOICE')")
    public ResponseEntity<InvoiceResponse> updateInvoice(@PathVariable UUID id, @Valid @RequestBody InvoiceUpdateRequest request) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_INVOICE')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}