package com.supermarket.salesmanagement.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;


@Configuration
public class FeignClientConfig {

    @Bean

    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                String jwtToken = (String) authentication.getCredentials();
                System.out.println("Adding JWT token to Feign request: {}" + jwtToken); // Add logging
                requestTemplate.header("Authorization", "Bearer " + jwtToken);
            } else {
                System.out.println("No valid authentication or credentials found in SecurityContextHolder");
            }
        };
    }
}
