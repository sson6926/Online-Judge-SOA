package com.shawnix.api_gateway.filter;

import com.shawnix.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Routes that don't require a token
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/users/login",
            "/api/users/signup",
            "/health",
            "/actuator/health"
    );

    // GET /api/problems is public (read-only)
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/problems"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // Allow public paths unconditionally
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = jwtUtil.extractBearerToken(authHeader);

        if (token == null || !jwtUtil.isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Inject user info into downstream headers
        Claims claims = jwtUtil.validateToken(token);
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Role", claims.get("role", String.class))
                )
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isPublicPath(String path, String method) {
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) return true;
        if ("GET".equals(method) && PUBLIC_GET_PATHS.stream().anyMatch(path::startsWith)) return true;
        return false;
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
