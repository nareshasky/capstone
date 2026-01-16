package com.virtusa.order.filter;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientHeaderForwardInterceptor implements RequestInterceptor {

    private static final List<String> HEADERS_TO_FORWARD =
        List.of("X-Internal-Gateway", "X-User-Id", "X-User-Role");

    @Override
    public void apply(RequestTemplate template) {

        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();

        HEADERS_TO_FORWARD.forEach(header -> {
            String value = request.getHeader(header);
            if (value != null) {
                template.header(header, value);
            }
        });
    }
}
