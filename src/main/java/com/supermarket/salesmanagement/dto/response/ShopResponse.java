package com.supermarket.salesmanagement.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShopResponse {
    private UUID id;
    private String name;
    private String location;
    private String managerContact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}