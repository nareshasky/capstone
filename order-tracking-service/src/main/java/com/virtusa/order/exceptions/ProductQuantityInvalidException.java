package com.virtusa.order.exceptions;

public class ProductQuantityInvalidException extends RuntimeException{

    public ProductQuantityInvalidException() {
        super();
    }

    public ProductQuantityInvalidException(String message) {
        super(message);
    }

    public ProductQuantityInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductQuantityInvalidException(Throwable cause) {
        super(cause);
    }

    protected ProductQuantityInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
