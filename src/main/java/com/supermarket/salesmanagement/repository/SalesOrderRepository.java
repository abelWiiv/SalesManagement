package com.supermarket.salesmanagement.repository;

import com.supermarket.salesmanagement.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
}