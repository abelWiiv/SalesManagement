package com.supermarket.salesmanagement.repository;

import com.supermarket.salesmanagement.model.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, UUID> {
    @Transactional
    @Modifying
    @Query("DELETE FROM SalesOrderItem soi WHERE soi.salesOrder.id = :salesOrderId")
    void deleteBySalesOrderId(UUID salesOrderId);
}