package com.supermarket.salesmanagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShopDto {

    private UUID id;
    private String name;
    private String location;
    private String managerContact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
