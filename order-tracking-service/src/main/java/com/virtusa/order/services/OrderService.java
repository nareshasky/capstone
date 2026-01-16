package com.virtusa.order.services;

import com.virtusa.order.dto.OrderRequest;
import com.virtusa.order.dto.OrderResponse;

public interface OrderService {
    public OrderResponse placeOrder(OrderRequest request);
    public OrderResponse getOrder(Long orderId);

    OrderResponse cancelOrder(Long orderId);
    OrderResponse completeOrder(Long orderId);
}
