package com.virtusa.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotNull
    private Long customerId;

    @NotEmpty
    private List<OrderItemRequest> items;
}
