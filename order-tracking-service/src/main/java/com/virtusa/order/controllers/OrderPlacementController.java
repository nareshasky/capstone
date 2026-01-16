package com.virtusa.order.controllers;

import com.virtusa.order.dto.OrderRequest;
import com.virtusa.order.dto.OrderResponse;
import com.virtusa.order.services.OrderService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderPlacementController {

    private final OrderService orderService;

    @PostMapping
    @Observed(name = "order-service-placeOrder", contextualName = "placing-order-controller")
    public ResponseEntity<OrderResponse> placeOrder(
            @Validated @RequestBody OrderRequest request) {
        log.info("place order called. {}", request);

        return new ResponseEntity<>(
                orderService.placeOrder(request),
                HttpStatus.CREATED
        );
    }

    // GET
    @GetMapping("/{orderId}")
    @Observed(name = "order-service-getOrder", contextualName = "getOrder-controller")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        log.info("getOrder mapping");
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    // CANCEL
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    // COMPLETE
    @PutMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.completeOrder(orderId));
    }
}
