package br.com.orbittapi.identity.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String GATEWAY_USER_HEADER = "X-User-Id";
    private static final String GATEWAY_ROLE_HEADER = "X-User-Role";

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String gatewayUserId = request.getHeader(GATEWAY_USER_HEADER);
            if (gatewayUserId != null && !gatewayUserId.isBlank()) {
                String role = request.getHeader(GATEWAY_ROLE_HEADER);
                authenticate(UUID.fromString(gatewayUserId), role == null ? "DEVELOPER" : role, request);
            } else {
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith(BEARER_PREFIX)) {
                    String token = header.substring(BEARER_PREFIX.length());
                    UUID accountId = tokenProvider.extractAccountId(token);
                    String role = tokenProvider.extractRole(token);
                    authenticate(accountId, role, request);
                }
            }
        } catch (Exception ex) {
            log.debug("JWT authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(UUID accountId, String role, HttpServletRequest request) {
        var authority = new SimpleGrantedAuthority("ROLE_" + role);
        var auth = new UsernamePasswordAuthenticationToken(accountId, null, List.of(authority));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
