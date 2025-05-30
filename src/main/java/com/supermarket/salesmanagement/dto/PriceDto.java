package com.supermarket.salesmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PriceDto {

    private UUID id;
    private UUID productId;
    private String customerCategory;
    private BigDecimal price;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
