package com.virtusa.order.dto;

import com.virtusa.order.entities.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderResponse {

    private Long orderId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private int totalItems;
    private List<String> productSummary;
    private LocalDateTime createdAt;
}
