package com.supermarket.salesmanagement.service.client;

import com.supermarket.salesmanagement.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "shop-management", url = "${application.shop.service.url}", configuration = FeignClientConfig.class)
public interface ShopClient {
    @GetMapping("/api/v1/shops/{id}")
    Object getShopById(@PathVariable("id") UUID id);
}