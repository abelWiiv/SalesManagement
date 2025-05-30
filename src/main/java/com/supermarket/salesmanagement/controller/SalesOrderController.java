package com.supermarket.salesmanagement.controller;

import com.supermarket.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderItemAddRequest;
import com.supermarket.salesmanagement.dto.request.SalesOrderUpdateRequest;
import com.supermarket.salesmanagement.dto.response.SalesOrderResponse;
import com.supermarket.salesmanagement.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {
    private final SalesOrderService salesOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_SALES_ORDER')")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(@Valid @RequestBody SalesOrderCreateRequest request) {
        return ResponseEntity.ok(salesOrderService.createSalesOrder(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_SALES_ORDER')")
    public ResponseEntity<SalesOrderResponse> getSalesOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_SALES_ORDER')")
    public ResponseEntity<Page<SalesOrderResponse>> getAllSalesOrders(Pageable pageable) {
        return ResponseEntity.ok(salesOrderService.getAllSalesOrders(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_SALES_ORDER')")
    public ResponseEntity<SalesOrderResponse> updateSalesOrder(@PathVariable UUID id, @Valid @RequestBody SalesOrderUpdateRequest request) {
        return ResponseEntity.ok(salesOrderService.updateSalesOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_SALES_ORDER')")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable UUID id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('UPDATE_SALES_ORDER')")
    public ResponseEntity<SalesOrderResponse> addSalesOrderItem(@PathVariable UUID id, @Valid @RequestBody SalesOrderItemAddRequest request) {
        return ResponseEntity.ok(salesOrderService.addSalesOrderItem(id, request));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('CONFIRM_SALES_ORDER')")
    public ResponseEntity<SalesOrderResponse> confirmSalesOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(salesOrderService.confirmOrderAfterPayment(id));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasAuthority('UPDATE_SALES_ORDER')")
    public ResponseEntity<SalesOrderResponse> deleteSalesOrderItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return ResponseEntity.ok(salesOrderService.deleteSalesOrderItem(orderId, itemId));
    }
}