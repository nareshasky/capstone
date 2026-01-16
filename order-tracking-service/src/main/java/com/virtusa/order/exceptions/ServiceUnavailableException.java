package com.virtusa.order.exceptions;

import com.virtusa.order.dto.ErrorResponseDto;
import lombok.Getter;

public class ServiceUnavailableException extends BaseServiceException {
    public ServiceUnavailableException(ErrorResponseDto error) {
        super(error);
    }
}
