package com.supermarket.salesmanagement.repository;

import com.supermarket.salesmanagement.model.Invoice;
import com.supermarket.salesmanagement.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    boolean existsBySalesOrderId(UUID salesOrderId);
    Optional<Invoice> findBySalesOrderId(UUID salesOrderId);
}