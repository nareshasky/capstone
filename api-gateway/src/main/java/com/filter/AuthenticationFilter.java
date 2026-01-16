package com.filter;


import com.error.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utility.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Environment environment;

//    @Autowired
//    private RoleRuleConfig roleRuleConfig;


    private ObjectMapper objectMapper;

    public AuthenticationFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return ((exchange, chain) -> {
            ServerHttpRequest mutatedRequest=null;
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (validator.isSecured.test(request)) {
                //header contains token or not
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
//                    throw new RuntimeException("missing authorization header");
                    return writeErrorResponse(
                            exchange,
                            HttpStatus.UNAUTHORIZED,
                            "Unauthorized",
                            "missing authorization header"
                    );
                }


                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {

                    // Validate token & extract claims
                    Claims claims = jwtUtil.validateToken(authHeader);

                    String username = claims.getSubject();
                    Integer userId = claims.get("id", Integer.class);
                    String role = claims.get("role", String.class);
                    String applicationName =
                            environment.getProperty("spring.application.name");
                    // ROLE CHECK
                    if (path.contains("/admin") && !"ADMIN".equals(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
                        return writeErrorResponse(
                                exchange,
                                HttpStatus.UNAUTHORIZED,
                                "Unauthorized",
                                "User not authorized to access this resource"
                        );
                    }

                    if ((path.startsWith("/products") || path.startsWith("/orders")) && !("USER".equals(role) || "ADMIN".equals(role))) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
                        return writeErrorResponse(
                                exchange,
                                HttpStatus.FORBIDDEN,
                                "Forbidden",
                                "User not authorized to access this resource"
                        );
                    }
                    // Request with headers
                     mutatedRequest = request
                            .mutate()
                            .header("X-User-Id", String.valueOf(userId))
                            .header("X-Username", username)
                            .header("X-Role", role)
                             .header("X-Internal-Gateway", applicationName)
                            .build();
                } catch (Exception e) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED,
                            "Unauthorized access");
                }
            }
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                               HttpStatus status,
                               String message) {

        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String error,
            String message) {

        ErrorResponseDto response = new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                error,
                message
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }



    public static class Config {

    }
}
