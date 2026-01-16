package com.virtusa.order.client;

import com.virtusa.order.dto.ProductResponseDto;
import com.virtusa.order.dto.StockUpdateRequest;
import com.virtusa.order.exceptions.ServiceUnavailableException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


//
//@Component
//public class ProductClientFallback implements ProductClient {
//
//    @Override
//    public ProductResponseDto getProduct(Long productId) {
//
//        throw new ServiceUnavailableException(
//                "Product Service is currently unavailable********");
//    }
//
//    @Override
//    public ProductResponseDto reduceStock(StockUpdateRequest request) {
//        throw new ServiceUnavailableException(
//                "Unable to update stock. Product Service unavailable");
//    }
//}
