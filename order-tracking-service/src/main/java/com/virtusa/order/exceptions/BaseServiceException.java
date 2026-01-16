package com.virtusa.order.exceptions;

import com.virtusa.order.dto.ErrorResponseDto;
import lombok.Getter;

@Getter
public abstract class BaseServiceException extends RuntimeException {

    private final ErrorResponseDto error;

    protected BaseServiceException(ErrorResponseDto error) {
        super(error.getMessage());
        this.error = error;
    }
}
