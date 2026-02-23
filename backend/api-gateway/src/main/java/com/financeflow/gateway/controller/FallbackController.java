package com.financeflow.gateway.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.warn("Auth service circuit breaker triggered - returning fallback response");
        return buildFallbackResponse("Auth Service", "Authentication service is temporarily unavailable");
    }

    @RequestMapping(value = "/account", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> accountServiceFallback() {
        log.warn("Account service circuit breaker triggered - returning fallback response");
        return buildFallbackResponse("Account Service", "Account service is temporarily unavailable");
    }

    @RequestMapping(value = "/transaction", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> transactionServiceFallback() {
        log.warn("Transaction service circuit breaker triggered - returning fallback response");
        return buildFallbackResponse("Transaction Service", "Transaction service is temporarily unavailable");
    }

    @RequestMapping(value = "/analytics", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> analyticsServiceFallback() {
        log.warn("Analytics service circuit breaker triggered - returning fallback response");
        return buildFallbackResponse("Analytics Service", "Analytics service is temporarily unavailable");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String service, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("service", service);
        response.put("fallback", true);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
