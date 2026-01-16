package com.virtusa.order.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtusa.order.dto.ErrorResponseDto;
import com.virtusa.order.dto.ProductResponseDto;
import com.virtusa.order.dto.StockUpdateRequest;
import com.virtusa.order.exceptions.ProductNotFoundException;
import com.virtusa.order.exceptions.ResourceNotFoundException;
import com.virtusa.order.exceptions.ServiceUnavailableException;
import feign.FeignException;
import feign.Request;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProductClientFallbackFactory
        implements FallbackFactory<ProductClient> {

    private final ObjectMapper objectMapper;

    // Inject Spring-managed ObjectMapper
    public ProductClientFallbackFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public ProductClient create(Throwable cause) {

        return new ProductClient() {

            @Override
            public ProductResponseDto increaseStock(StockUpdateRequest request) {
                log.error("Feign increaseStock failed", cause);
                throw mapToException(cause);
            }
            @Override
            public ProductResponseDto getProduct(Long productId) {
                log.error("Feign getProduct failed for productId={}", productId, cause);
                throw mapToException(cause);
            }

            @Override
            public ProductResponseDto reduceStock(StockUpdateRequest request) {
                log.error(
                        "Feign reduceStock failed for productId={}, qty={}",
                        request.getProductId(),
                        request.getQuantity(),
                        cause
                );
                throw mapToException(cause);
            }

            private RuntimeException mapToException(Throwable cause) {

                if (cause instanceof FeignException fe) {

                    ErrorResponseDto error = extractErrorResponse(fe);

                    // ONLY for 404
                    if (fe.status() == HttpStatus.NOT_FOUND.value()) {
                        return new ProductNotFoundException(error);
                    }

                    // other Feign errors (400 / 500 / etc.)
                    return new ResourceNotFoundException(error);
                }

                // service down / timeout / circuit open
                return new ServiceUnavailableException(
                        new ErrorResponseDto(
                                LocalDateTime.now(),
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                "SERVICE_UNAVAILABLE",
                                "Product Service is currently unavailable",
                                "/null"
                        )
                );
            }

            private ErrorResponseDto extractErrorResponse(FeignException fe) {

                try {
                    if (fe.contentUTF8() != null && !fe.contentUTF8().isBlank()) {
                        int status = fe.status();
                        String data1=fe.contentUTF8();
                        ByteBuffer byteBuffer = fe.responseBody().orElse(null);
                        String url=fe.request().url();
                        Request.HttpMethod httpMethod = fe.request().httpMethod();
                        Map<String, Collection<String>> headers=fe.request().headers();
                        Map<String, Collection<String>> respheaders=fe.responseHeaders();
                        String message = fe.getMessage();
                        return objectMapper.readValue(
                                fe.contentUTF8(),
                                ErrorResponseDto.class
                        );
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse Product-Service error response", e);
                }

                return buildServiceUnavailableError(fe);
            }

            private ErrorResponseDto buildServiceUnavailableError(Throwable cause) {

                String path = "/null";

                if (cause instanceof FeignException fe && fe.request() != null) {
                    path = fe.request().url(); // dynamic
                }

                return new ErrorResponseDto(
                        LocalDateTime.now(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "SERVICE_UNAVAILABLE",
                        "Product Service is currently unavailable",
                        path
                );
            }
        };
    }
}
