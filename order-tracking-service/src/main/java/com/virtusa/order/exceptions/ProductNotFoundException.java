package com.virtusa.order.exceptions;

import com.virtusa.order.dto.ErrorResponseDto;
import lombok.Getter;

public class ProductNotFoundException extends BaseServiceException {
    public ProductNotFoundException(ErrorResponseDto error) {
        super(error);
    }
}
