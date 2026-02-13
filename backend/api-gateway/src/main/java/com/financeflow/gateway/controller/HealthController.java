package com.financeflow.gateway.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "FinanceFlow API Gateway");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "UP");
        response.put("endpoints", Map.of(
            "auth", "/api/auth/**",
            "accounts", "/api/accounts/**",
            "transactions", "/api/transactions/**",
            "analytics", "/api/analytics/**"
        ));
        return ResponseEntity.ok(response);
    }
}
