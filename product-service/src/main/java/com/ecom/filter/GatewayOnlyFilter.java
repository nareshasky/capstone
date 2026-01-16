package com.ecom.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class GatewayOnlyFilter extends OncePerRequestFilter {

    private static final String INTERNAL_HEADER = "X-Internal-Gateway";
    private static final String INTERNAL_VALUE = "API-GATEWAY";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String gatewayHeader = request.getHeader(INTERNAL_HEADER);
        String path = request.getRequestURI();

        final List<String> OPEN_API_PATHS = List.of(
                "/swagger",
                "/v3/api-docs",
                "/swagger-ui"
        );

        boolean isAllow = OPEN_API_PATHS.stream().anyMatch(path::contains);


        if (!isAllow && !INTERNAL_VALUE.equals(gatewayHeader)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Direct access forbidden");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
