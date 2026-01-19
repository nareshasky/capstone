package com.virtusa.order.services.impl;

import com.virtusa.order.client.ProductClient;
import com.virtusa.order.dto.*;
import com.virtusa.order.dto.OrderItemRequest;
import com.virtusa.order.dto.OrderRequest;
import com.virtusa.order.dto.OrderResponse;
import com.virtusa.order.dto.ProductResponseDto;
import com.virtusa.order.entities.Order;
import com.virtusa.order.entities.OrderItem;
import com.virtusa.order.entities.OrderStatus;
import com.virtusa.order.exceptions.*;
import com.virtusa.order.repositories.OrderRepository;
import com.virtusa.order.services.OrderService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;



    @Transactional
    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public OrderResponse placeOrder(OrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest item : request.getItems()) {
            log.info("Fetching product details for ID: {}", item.getProductId());
            ProductResponseDto product =
                    productClient.getProduct(item.getProductId());
            if (product==null) {
                throw new ProductIdNotFoundException(
                        "Product not found! ID: " + item.getProductId());
            }

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new InvalidOrderException(
                        "Product is inactive: " + product.getName());
            }

            if (item.getQuantity()==0) {
                throw new ProductQuantityInvalidException(
                        "Product Quantity Invalid, Quantity: " + 0);
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
            }


            // Reduce stock in PRODUCT-SERVICE
            productClient.reduceStock(
                    new StockUpdateRequest(item.getProductId(), item.getQuantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(item.getQuantity())
                    .build();

            orderItems.add(orderItem);

            totalAmount = totalAmount.add(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return saveOrder(
                request.getCustomerId(),
                orderItems,
                totalAmount
        );
    }


//    SAVE ORDER (REQUIRED)
    private OrderResponse saveOrder(
            Long customerId,
            List<OrderItem> orderItems,
            BigDecimal totalAmount) {

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.CREATED)
                .totalAmount(totalAmount)
                .items(orderItems)
                .build();

        // Set bidirectional mapping
        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        return buildOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new InvalidOrderException("Order not found"));

        return buildOrderResponse(order);
    }


 //          JAVA 8 STREAMS SUMMARY
    private OrderResponse buildOrderResponse(Order order) {

        List<String> productSummary = order.getItems()
                .stream()
                .map(item ->
                        item.getProductName() + " x " + item.getQuantity())
                .toList();

        int totalItems = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(totalItems)
                .productSummary(productSummary)
                .createdAt(order.getCreatedAt())
                .build();
    }


  //   CIRCUIT BREAKER FALLBACK

    public OrderResponse productFallback(
            OrderRequest request,
            Throwable ex) {

        //  Propagate domain exceptions as-is
        if (ex instanceof BaseServiceException baseException) {
            throw baseException;
        }

        //  Feign exception not converted yet
        if (ex instanceof FeignException fe) {
            throw new ServiceUnavailableException(
                    new ErrorResponseDto(
                            LocalDateTime.now(),
                            HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "SERVICE_UNAVAILABLE",
                            "Product Service is currently unavailable",
                            null // path will be set in ControllerAdvice
                    )
            );
        }

        //  Circuit breaker open / timeout / unknown
        throw new ServiceUnavailableException(
                new ErrorResponseDto(
                        LocalDateTime.now(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "SERVICE_UNAVAILABLE",
                        "Order service is temporarily unavailable. Please try again later.",
                        null
                )
        );
    }

//    @Override
//    @Transactional
//    public OrderResponse cancelOrder(Long orderId) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() ->
//                        new InvalidOrderException("Order not found"));
//
//        if (order.getStatus() == OrderStatus.CANCELLED) {
//            throw new InvalidOrderException("Order already cancelled");
//        }
//
//        if (order.getStatus() == OrderStatus.COMPLETED) {
//            throw new InvalidOrderException("Completed order cannot be cancelled");
//        }
//
//        order.setStatus(OrderStatus.CANCELLED);
//
//        Order updatedOrder = orderRepository.save(order);
//        return buildOrderResponse(updatedOrder);
//    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new InvalidOrderException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order already cancelled");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Completed order cannot be cancelled");
        }

        // REVERT STOCK
        for (OrderItem item : order.getItems()) {

            productClient.increaseStock(
                    new StockUpdateRequest(
                            item.getProductId(),
                            item.getQuantity()
                    )
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        return buildOrderResponse(updatedOrder);
    }


    @Override
    @Transactional
    public OrderResponse completeOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new InvalidOrderException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Cancelled order cannot be completed");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Order already completed");
        }

        order.setStatus(OrderStatus.COMPLETED);

        Order updatedOrder = orderRepository.save(order);
        return buildOrderResponse(updatedOrder);
    }

}
