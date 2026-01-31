package com.financeflow.gateway.filter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.financeflow.gateway.config.RouteConfig;
import com.financeflow.gateway.service.JwtService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private RouteConfig routeConfig;

    @BeforeEach
    void setUp() {
        routeConfig = new RouteConfig();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, routeConfig);
    }

    @Test
    void shouldAllowPublicEndpointsWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn("user-123");
        when(jwtService.extractEmail("valid-token")).thenReturn("test@example.com");
        when(filterChain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    @Test
    void shouldRejectRequestWithMalformedAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "InvalidHeader")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }
}
