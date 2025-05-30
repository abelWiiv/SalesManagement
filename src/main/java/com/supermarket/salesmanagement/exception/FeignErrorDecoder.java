
package com.supermarket.salesmanagement.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = "Unknown error";
        try {
            if (response.body() != null) {
                errorMessage = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            // Log the error if reading the response body fails
        }

        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            return new CustomException("Resource not found: " + errorMessage);
        }
        if (response.status() >= 400 && response.status() <= 499) {
            return new CustomException("Client error: " + errorMessage);
        }
        if (response.status() >= 500) {
            return new CustomException("Server error: " + errorMessage);
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
//package com.supermarket.salesmanagement.exception;
//
//import feign.Response;
//import feign.codec.ErrorDecoder;
//import org.springframework.http.HttpStatus;
//
//public class FeignErrorDecoder implements ErrorDecoder {
//    private final ErrorDecoder defaultErrorDecoder = new Default();
//
//    @Override
//    public Exception decode(String methodKey, Response response) {
//        if (response.status() == HttpStatus.NOT_FOUND.value()) {
//            return new CustomException("Resource not found: " + response.reason());
//        }
//        if (response.status() >= 400 && response.status() <= 499) {
//            return new CustomException("Client error: " + response.reason());
//        }
//        if (response.status() >= 500) {
//            return new CustomException("Server error: " + response.reason());
//        }
//        return defaultErrorDecoder.decode(methodKey, response);
//    }
//}