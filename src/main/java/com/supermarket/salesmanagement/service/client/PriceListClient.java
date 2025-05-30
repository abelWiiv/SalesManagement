//package com.supermarket.salesmanagement.service.client;
//
//import com.supermarket.salesmanagement.config.FeignClientConfig;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@FeignClient(name = "price-list-service", url = "${application.price.service.url}", configuration = FeignClientConfig.class)
//public interface PriceListClient {
//    @GetMapping("/api/v1/prices/{productId}")
//    BigDecimal getProductPrice(@PathVariable("productId") UUID productId);
//}to