package com.supermarket.salesmanagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerDto {

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
