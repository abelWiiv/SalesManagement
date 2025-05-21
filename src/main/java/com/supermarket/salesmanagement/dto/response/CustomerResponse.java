package com.supermarket.salesmanagement.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerResponse {
    private UUID id;
    private String companyName;
    private String contactPerson;
    private String address;
    private String email;
    private String phoneNumber;
    private String vatRegistrationNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}