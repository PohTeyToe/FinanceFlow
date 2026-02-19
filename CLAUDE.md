# FinanceFlow

Microservices banking platform with Spring Boot, React, and PostgreSQL.

## Services
- auth-service (8081) — JWT authentication with refresh token rotation
- account-service (8082) — Account CRUD with pessimistic locking
- transaction-service (8083) — Deposits, withdrawals, transfers
- analytics-service (8084) — Spending analytics with Redis caching
- api-gateway (8080) — Spring Cloud Gateway with circuit breakers

## Running locally
docker-compose up -d

## Running tests
cd backend && mvn test
