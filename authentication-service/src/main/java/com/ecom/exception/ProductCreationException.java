package com.ecom.exception;

public class ProductCreationException extends RuntimeException {
    public ProductCreationException() {
        super();
    }

    public ProductCreationException(String message) {
        super(message);
    }

    public ProductCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductCreationException(Throwable cause) {
        super(cause);
    }

    protected ProductCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
