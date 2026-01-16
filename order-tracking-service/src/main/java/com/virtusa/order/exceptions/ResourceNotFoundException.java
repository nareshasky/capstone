package com.virtusa.order.exceptions;

import com.virtusa.order.dto.ErrorResponseDto;
import lombok.Getter;

public class ResourceNotFoundException extends BaseServiceException {
    public ResourceNotFoundException(ErrorResponseDto error) {
        super(error);
    }
}
