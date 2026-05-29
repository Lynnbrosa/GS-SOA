package br.com.orbittapi.gateway.filter;

import br.com.orbittapi.gateway.config.JwtProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-User-Role";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui"
    );

    private final SecretKey signingKey;
    private final String issuer;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationGatewayFilter(JwtProperties properties, ObjectMapper objectMapper) {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("orbittapi.jwt.secret must be at least 32 bytes for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = properties.getIssuer();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Deixa CORS preflight passar: Spring Cloud Gateway responde com base no globalcors.
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "missing-token", "Missing or invalid Authorization header");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID accountId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);
            if (role == null) role = "DEVELOPER";

            ServerHttpRequest mutated = request.mutate()
                    .header(USER_HEADER, accountId.toString())
                    .header(ROLE_HEADER, role)
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return unauthorized(exchange, "invalid-token", "Invalid or expired JWT");
        }
    }

    private boolean isPublic(String path) {
        for (String prefix : PUBLIC_PATHS) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String slug, String detail) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", "https://orbittapi.dev/errors/" + slug);
        problem.put("title", "Unauthorized");
        problem.put("status", 401);
        problem.put("detail", detail);
        problem.put("instance", URI.create(exchange.getRequest().getURI().getPath()).toString());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(problem);
        } catch (JsonProcessingException e) {
            bytes = ("{\"title\":\"Unauthorized\",\"status\":401}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
