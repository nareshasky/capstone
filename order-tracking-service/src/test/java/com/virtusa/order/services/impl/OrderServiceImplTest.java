package com.virtusa.order.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.virtusa.order.client.ProductClient;
import com.virtusa.order.dto.*;
import com.virtusa.order.entities.Order;
import com.virtusa.order.entities.OrderItem;
import com.virtusa.order.entities.OrderStatus;
import com.virtusa.order.exceptions.InsufficientStockException;
import com.virtusa.order.exceptions.InvalidOrderException;
import com.virtusa.order.exceptions.ServiceUnavailableException;
import com.virtusa.order.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;


      // placeOrder() – SUCCESS


    @Test
    void placeOrder_success() {

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(100L);
        orderRequest.setItems(List.of(itemRequest));

        ProductResponseDto product = ProductResponseDto.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(50000))
                .stockQuantity(10)
                .active(true)
                .build();

        when(productClient.getProduct(1L))
                .thenReturn(product);

        doNothing().when(productClient)
                .reduceStock(any(StockUpdateRequest.class));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        OrderResponse response =
                orderService.placeOrder(orderRequest);

        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertEquals(2, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(100000), response.getTotalAmount());

        verify(productClient).getProduct(1L);
        verify(productClient).reduceStock(any(StockUpdateRequest.class));
        verify(orderRepository).save(any(Order.class));
    }


       //placeOrder() – EMPTY ITEMS

    @Test
    void placeOrder_emptyItems_shouldThrowException() {

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(100L);
        orderRequest.setItems(List.of());

        assertThrows(InvalidOrderException.class,
                () -> orderService.placeOrder(orderRequest));

        verifyNoInteractions(productClient);
        verifyNoInteractions(orderRepository);
    }

      // placeOrder() – PRODUCT INACTIVE

    @Test
    void placeOrder_productInactive() {

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(100L);
        orderRequest.setItems(List.of(itemRequest));

        ProductResponseDto product = ProductResponseDto.builder()
                .id(1L)
                .name("Laptop")
                .active(false)
                .build();

        when(productClient.getProduct(1L))
                .thenReturn(product);

        assertThrows(InvalidOrderException.class,
                () -> orderService.placeOrder(orderRequest));

        verify(productClient).getProduct(1L);
        verify(productClient, never())
                .reduceStock(any());
        verify(orderRepository, never()).save(any());
    }

      // placeOrder() – INSUFFICIENT STOCK

    @Test
    void placeOrder_insufficientStock() {

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(10);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(100L);
        orderRequest.setItems(List.of(itemRequest));

        ProductResponseDto product = ProductResponseDto.builder()
                .id(1L)
                .name("Laptop")
                .stockQuantity(5)
                .active(true)
                .build();

        when(productClient.getProduct(1L))
                .thenReturn(product);

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(orderRequest));

        verify(productClient).getProduct(1L);
        verify(productClient, never())
                .reduceStock(any());
        verify(orderRepository, never()).save(any());
    }


       //getOrder()

    @Test
    void getOrder_success() {

        OrderItem item = OrderItem.builder()
                .productName("Mouse")
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(2000))
                .items(List.of(item))
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.getOrder(1L);

        assertEquals(1L, response.getOrderId());
        assertEquals(2, response.getTotalItems());
        assertEquals("Mouse x 2",
                response.getProductSummary().get(0));
    }

    @Test
    void getOrder_notFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(InvalidOrderException.class,
                () -> orderService.getOrder(1L));
    }

    //   CIRCUIT BREAKER FALLBACK

    @Test
    void productFallback_shouldThrowServiceUnavailable() {

        OrderRequest request = new OrderRequest();

        assertThrows(ServiceUnavailableException.class,
                () -> orderService.productFallback(
                        request, new RuntimeException()));
    }
}
