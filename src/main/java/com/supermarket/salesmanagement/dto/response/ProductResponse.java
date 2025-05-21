package com.supermarket.salesmanagement.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID categoryId;
    private UUID supplierId;
    private String unitOfMeasure;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}