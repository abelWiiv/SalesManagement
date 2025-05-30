package com.supermarket.salesmanagement.service.client;

import com.supermarket.salesmanagement.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "customer-management", url = "${application.customer.service.url}", configuration = FeignClientConfig.class)
public interface CustomerClient {
    @GetMapping("/api/v1/customers/{id}")
    Object getCustomerById(@PathVariable("id") UUID id);
}