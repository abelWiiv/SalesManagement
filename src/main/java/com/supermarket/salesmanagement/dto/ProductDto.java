package com.supermarket.salesmanagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDto {

    private UUID id;
    private String name;
    private String description;
    private UUID categoryId;
    private UUID supplierId;
    private String unitOfMeasure;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
