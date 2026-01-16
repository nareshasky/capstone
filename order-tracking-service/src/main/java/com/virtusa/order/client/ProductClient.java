package com.virtusa.order.client;

import com.virtusa.order.dto.ProductResponseDto;
import com.virtusa.order.dto.StockUpdateRequest;
import io.micrometer.observation.annotation.Observed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "PRODUCT-SERVICE",
        path = "/products",
        fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {

    @GetMapping("/{productId}")
    ProductResponseDto getProduct(@PathVariable("productId") Long productId);

    @PutMapping("/reduceStock")
    ProductResponseDto reduceStock(@RequestBody StockUpdateRequest request);

    @PutMapping("/increaseStock")
    ProductResponseDto increaseStock(@RequestBody StockUpdateRequest request);
}
