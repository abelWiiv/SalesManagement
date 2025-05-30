package com.supermarket.salesmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SalesmanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesmanagementApplication.class, args);
	}

}
