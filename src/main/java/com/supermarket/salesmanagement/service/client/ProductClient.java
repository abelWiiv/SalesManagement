package com.supermarket.salesmanagement.service.client;

import com.supermarket.salesmanagement.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-management", url = "${application.product.service.url}", configuration = FeignClientConfig.class)
public interface ProductClient {
    @GetMapping("/api/v1/products/{id}")
    Object getProductById(@PathVariable("id") UUID id);
}