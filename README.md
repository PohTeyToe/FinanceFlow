<div align="center">

# FinanceFlow

### Full-Stack Banking Platform

[![CI/CD Pipeline](https://github.com/PohTeyToe/FinanceFlow/actions/workflows/ci.yml/badge.svg)](https://github.com/PohTeyToe/FinanceFlow/actions)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat&logo=redis&logoColor=white)](https://redis.io/)
[![Terraform](https://img.shields.io/badge/Terraform-AWS-844FBA?style=flat&logo=terraform&logoColor=white)](https://www.terraform.io/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Manifests-326CE5?style=flat&logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**A full-stack microservices banking platform built with Spring Boot, React, and PostgreSQL.**

[Features](#features) • [Architecture](#architecture) • [Quick Start](#quick-start) • [API Reference](#api-reference) • [Tech Stack](#tech-stack)

---

</div>

## Why I Built This

I wanted to build something that goes beyond a basic CRUD app and actually deals with the kinds of problems you'd hit in a real banking system -- things like concurrent balance updates, atomic transfers, and keeping multiple services in sync.

The pessimistic locking on balance updates was something I had to debug for hours before getting right. Turns out it's easy to write a transfer endpoint that passes all your tests but breaks under concurrent load. I also underestimated how much work goes into a proper JWT auth flow with refresh token rotation -- the edge cases around token expiry and race conditions between tabs were tricky.

---

## Screenshots

| Login | Dashboard |
|-------|-----------|
| ![Login](docs/screenshots/login.png) | ![Dashboard](docs/screenshots/dashboard.png) |

| Accounts | Transactions |
|----------|--------------|
| ![Accounts](docs/screenshots/accounts.png) | ![Transactions](docs/screenshots/transactions.png) |

| Analytics |
|-----------|
| ![Analytics](docs/screenshots/analytics.png) |

---

## Skills Demonstrated

| Domain | Technologies & Concepts |
|--------|-------------------------|
| **Backend** | Java 17, Spring Boot 3, Spring Security 6, JPA/Hibernate, REST APIs |
| **Architecture** | Microservices, API Gateway, Circuit Breakers, Service Layer Pattern |
| **Security** | JWT Authentication, Refresh Token Rotation, BCrypt Hashing, CORS |
| **Frontend** | React 18, TypeScript, TanStack Query, Material UI, Recharts |
| **Caching** | Redis 7, Spring Cache (@Cacheable), TTL-based eviction |
| **DevOps** | Docker, Docker Compose, GitHub Actions CI/CD, Multi-stage Builds |
| **Cloud/IaC** | Terraform (AWS ECS Fargate, RDS, ALB, VPC), Kubernetes manifests |
| **Database** | PostgreSQL, Pessimistic Locking, Atomic Transactions, JPQL |

---

## Features

<table>
<tr>
<td width="50%">

### Authentication & Security
- JWT-based stateless authentication
- Refresh token rotation for security
- BCrypt password hashing
- Protected routes & API endpoints

### Account Management
- Multiple account types (Checking, Savings, Credit)
- Real-time balance tracking
- Auto-generated account numbers
- Soft delete for data retention

</td>
<td width="50%">

### Transaction Processing
- Deposits, withdrawals, transfers
- **Pessimistic locking** prevents race conditions
- **Atomic transfers** - both succeed or both fail
- Unique reference numbers (TXN-YYYY-NNNNNN)
- Advanced filtering & pagination

### Analytics Dashboard
- Spending by category breakdown
- Monthly trend analysis
- Income vs. expenses comparison
- Interactive charts with Recharts

</td>
</tr>
</table>

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                FRONTEND                                     │
│                     React 18 + TypeScript + Material UI                     │
│                              Port: 3000                                     │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ HTTP/REST
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY                                      │
│                    Spring Cloud Gateway (Port: 8080)                        │
│         ┌──────────────┬──────────────┬──────────────┬──────────────┐       │
│         │   Routing    │  JWT Filter  │    CORS      │   Circuit    │       │
│         │              │              │   Config     │   Breaker    │       │
│         └──────────────┴──────────────┴──────────────┴──────────────┘       │
└────────┬──────────────┬──────────────┬──────────────┬───────────────────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│    AUTH     │ │   ACCOUNT   │ │ TRANSACTION │ │  ANALYTICS  │
│   SERVICE   │ │   SERVICE   │ │   SERVICE   │ │   SERVICE   │
│   (8081)    │ │   (8082)    │ │   (8083)    │ │   (8084)    │
├─────────────┤ ├─────────────┤ ├─────────────┤ ├─────────────┤
│ • Register  │ │ • Create    │ │ • Deposit   │ │ • Spending  │
│ • Login     │ │ • List      │ │ • Withdraw  │ │   by Cat.   │
│ • JWT Gen   │ │ • Balance   │ │ • Transfer  │ │ • Monthly   │
│ • Refresh   │ │ • Update    │ │ • History   │ │   Trends    │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │               │               │               │
       └───────────────┴───────────────┴───────────────┘
                               │
                    ┌──────────┼──────────┐
                    ▼                     ▼
         ┌─────────────────────┐ ┌──────────────────────┐
         │     PostgreSQL      │ │       Redis           │
         │     (Port: 5432)    │ │     (Port: 6379)      │
         │                     │ │                       │
         │  • users            │ │  • Analytics cache    │
         │  • refresh_tokens   │ │  • TTL eviction       │
         │  • accounts         │ │  • LRU policy         │
         │  • transactions     │ │                       │
         └─────────────────────┘ └───────────────────────┘
```

### Key Design Patterns

| Pattern | Implementation |
|---------|----------------|
| **API Gateway** | Single entry point, JWT validation, rate limiting, circuit breakers |
| **DTO Pattern** | Separate domain models from API contracts |
| **Repository Pattern** | Abstract data access with Spring Data JPA |
| **Service Layer** | Business logic encapsulation |
| **Global Exception Handling** | Consistent error responses across services |
| **Pessimistic Locking** | `@Lock(PESSIMISTIC_WRITE)` for balance updates |
| **Atomic Transactions** | `@Transactional` for multi-account transfers |

---

## Quick Start

### Prerequisites

- **Docker Desktop** ([Download](https://www.docker.com/products/docker-desktop/)) - Make sure it's **running**
- 8GB+ free disk space
- Ports 3000, 5432, 8080-8084 available

### One-Command Setup

```bash
# Clone the repository
git clone https://github.com/PohTeyToe/FinanceFlow.git
cd FinanceFlow

# Start all services (first run takes ~5 minutes)
docker compose up --build
```

### Access the Application

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | React web application |
| **API Gateway** | http://localhost:8080 | Main API entry point |

### Demo Credentials

```
Email:    john.doe@example.com
Password: password123
```

> **Windows Users:** Use `docker compose` (with space), not `docker-compose` (hyphen).

---

## API Reference

All endpoints are accessed through the **API Gateway** at `http://localhost:8080`.

### Authentication

```http
POST /api/auth/register    # Create new account
POST /api/auth/login       # Get JWT tokens
POST /api/auth/refresh     # Refresh access token
GET  /api/auth/me          # Get current user (Protected)
```

<details>
<summary><b>Example: Login Request</b></summary>

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com", "password": "password123"}'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```
</details>

### Accounts

```http
GET    /api/accounts           # List user's accounts (Protected)
GET    /api/accounts/{id}      # Get account details (Protected)
POST   /api/accounts           # Create new account (Protected)
GET    /api/accounts/{id}/balance  # Get balance (Protected)
```

### Transactions

```http
GET    /api/transactions              # List with filters (Protected)
POST   /api/transactions/deposit      # Make deposit (Protected)
POST   /api/transactions/withdraw     # Make withdrawal (Protected)
POST   /api/transactions/transfer     # Transfer funds (Protected)
```

<details>
<summary><b>Query Parameters for GET /api/transactions</b></summary>

| Parameter | Type | Description |
|-----------|------|-------------|
| `accountId` | UUID | **Required** - Account to query |
| `type` | string | Filter: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT |
| `category` | string | Filter by category |
| `startDate` | ISO date | Filter from date |
| `endDate` | ISO date | Filter to date |
| `page` | int | Page number (0-indexed) |
| `size` | int | Page size (default: 20) |

</details>

### Analytics

```http
GET /api/analytics/spending-by-category  # Spending breakdown (Protected)
GET /api/analytics/monthly-trend         # Monthly trends (Protected)
GET /api/analytics/summary               # Account summary (Protected)
GET /api/analytics/income-vs-expenses    # Income comparison (Protected)
```

---

## Tech Stack

<table>
<tr>
<td valign="top" width="50%">

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 LTS | Core language |
| Spring Boot | 3.2.x | Application framework |
| Spring Security | 6.x | Authentication |
| Spring Data JPA | 3.2.x | Data persistence |
| Spring Cloud Gateway | 4.1.x | API Gateway |
| Resilience4j | 2.x | Circuit breakers |
| PostgreSQL | 15 | Database |
| Lombok | 1.18.x | Boilerplate reduction |
| JJWT | 0.12.x | JWT handling |
| JUnit 5 | 5.x | Testing |
| JaCoCo | 0.8.x | Code coverage |

</td>
<td valign="top" width="50%">

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.x | UI framework |
| TypeScript | 5.x | Type safety |
| Vite | 5.x | Build tool |
| TanStack Query | 5.x | Server state |
| React Router | 6.x | Routing |
| Material UI | 5.x | Components |
| Recharts | 2.x | Charts |
| Axios | 1.x | HTTP client |

### Caching

| Technology | Version | Purpose |
|------------|---------|---------|
| Redis | 7.x | Analytics caching |
| Spring Cache | 3.2.x | Cache abstraction |

### DevOps & Infrastructure

| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Orchestration |
| GitHub Actions | CI/CD |
| Nginx | Frontend serving |
| Terraform | AWS IaC (ECS, RDS, ALB) |
| Kubernetes | Container orchestration |

</td>
</tr>
</table>

---

## Project Structure

```
FinanceFlow/
├── docker-compose.yml          # Full stack orchestration
├── README.md
│
├── terraform/                  # AWS infrastructure (ECS, RDS, ALB, VPC)
│   ├── main.tf
│   ├── modules/
│   │   ├── vpc/                # VPC, subnets, security groups
│   │   ├── ecs/                # ECS Fargate, ECR, task definitions
│   │   ├── rds/                # RDS PostgreSQL
│   │   └── alb/                # Application Load Balancer
│   └── environments/
│
├── k8s/                        # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── ingress.yaml
│   ├── hpa.yaml
│   └── {service}/              # Per-service deployment + service
│
├── backend/
│   ├── pom.xml                 # Parent Maven POM
│   ├── init-db.sql             # Schema + seed data (50+ transactions)
│   │
│   ├── api-gateway/            # Spring Cloud Gateway
│   │   ├── filter/             # JWT validation, logging
│   │   └── config/             # Routes, CORS, circuit breakers
│   │
│   ├── auth-service/           # Authentication microservice
│   │   ├── controller/         # REST endpoints
│   │   ├── service/            # JWT generation, user auth
│   │   ├── model/              # User, RefreshToken entities
│   │   └── config/             # Security configuration
│   │
│   ├── account-service/        # Account management
│   │   ├── controller/         # CRUD endpoints
│   │   ├── service/            # Business logic
│   │   └── model/              # Account entity
│   │
│   ├── transaction-service/    # Transaction processing
│   │   ├── controller/         # Deposit, withdraw, transfer
│   │   ├── service/            # Atomic operations, locking
│   │   └── model/              # Transaction entity
│   │
│   └── analytics-service/      # Spending analytics + Redis caching
│       ├── controller/         # Analytics endpoints
│       ├── service/            # Aggregation queries
│       └── config/             # RedisConfig, cache management
│
└── frontend/
    ├── src/
    │   ├── components/         # Reusable UI components
    │   │   ├── Layout/         # App shell, navigation
    │   │   ├── Charts/         # Recharts visualizations
    │   │   └── common/         # Loading, errors, guards
    │   ├── pages/              # Route components
    │   ├── hooks/              # Custom React hooks
    │   ├── services/           # API layer
    │   ├── context/            # Auth, theme providers
    │   └── types/              # TypeScript definitions
    ├── Dockerfile              # Multi-stage build
    └── nginx.conf              # Production config
```

---

## Testing

```bash
# Run all backend tests
cd backend && mvn test

# Run with coverage report
mvn test jacoco:report

# Run frontend tests
cd frontend && npm test
```

**Coverage Target:** 70%+ on business logic

---

## Deployment Options

### Docker Compose (Local)

The default setup using `docker compose up --build`. See [Quick Start](#quick-start).

### Kubernetes

Kubernetes manifests in the `k8s/` directory with Deployments, Services, ConfigMap, Secrets, Ingress, and HPA.

```bash
# Apply all manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/

# Verify pods are running
kubectl get pods -n financeflow
```

See [`k8s/README.md`](k8s/README.md) for full instructions.

### AWS with Terraform

Infrastructure-as-code in `terraform/` using ECS Fargate, RDS PostgreSQL, ALB, and VPC.

```bash
cd terraform
terraform init
terraform plan -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars
```

Estimated cost: ~$115/month for dev environment. See [`terraform/README.md`](terraform/README.md) for details.

---

## API Documentation

Each service exposes interactive Swagger UI documentation:

| Service | Swagger UI URL |
|-|-|
| Auth Service | http://localhost:8081/api/auth/swagger-ui.html |
| Account Service | http://localhost:8082/api/accounts/swagger-ui.html |
| Transaction Service | http://localhost:8083/api/transactions/swagger-ui.html |
| Analytics Service | http://localhost:8084/api/analytics/swagger-ui.html |

All endpoints are annotated with `@Operation` and `@Tag` from SpringDoc OpenAPI.

---

## Local Development

<details>
<summary><b>Running without Docker</b></summary>

### Backend

```bash
# Start only PostgreSQL
docker compose up -d postgres

# Build all services
cd backend && mvn clean install -DskipTests

# Run each service (separate terminals)
cd auth-service && mvn spring-boot:run
cd account-service && mvn spring-boot:run
cd transaction-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

</details>

---

## Troubleshooting

<details>
<summary><b>"docker-compose is not recognized" (Windows)</b></summary>

Use `docker compose` (with space) instead of `docker-compose`:
```powershell
docker compose up --build
```
</details>

<details>
<summary><b>Port already in use</b></summary>

```bash
# Find process using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # macOS/Linux

# Or stop all Docker containers
docker compose down
```
</details>

<details>
<summary><b>Full reset</b></summary>

```bash
# Remove everything and rebuild
docker compose down -v --rmi local
docker compose up --build
```
</details>

---

## What I Learned

Building this taught me that distributed transactions are way harder than they seem. The transfer endpoint needs to lock both accounts, update both balances, and create two transaction records atomically -- and if you get the lock ordering wrong, you deadlock. I spent a while figuring out that `@Lock(PESSIMISTIC_WRITE)` only works if you're consistent about which account you lock first.

The API Gateway was another area where I learned a lot by getting it wrong first. My initial approach had the gateway re-validating JWTs on every request by calling the auth service, which defeated the whole point of stateless tokens. Refactoring to validate the signature locally in a filter was a good lesson in understanding what "stateless" actually means.

Redis caching for the analytics endpoints was straightforward to add with `@Cacheable`, but figuring out when to invalidate was the real problem. Right now the TTLs are a compromise -- short enough that data stays reasonably fresh, long enough that the DB isn't hammered. It's not perfect but it works for the scale this runs at.

On the DevOps side, writing the Terraform modules for AWS (ECS Fargate, RDS, ALB) taught me how much infrastructure config is really about networking and security groups. The actual service deployment was maybe 20% of the work -- the rest was VPC layout, subnet routing, and IAM policies.

---

## Roadmap

- [x] Auth Service with JWT & refresh tokens
- [x] Account Service with CRUD operations
- [x] Transaction Service with atomic transfers
- [x] Analytics Service with aggregations
- [x] API Gateway with circuit breakers
- [x] React TypeScript frontend
- [x] Docker Compose orchestration
- [x] GitHub Actions CI/CD
- [x] Swagger/OpenAPI documentation
- [x] Redis caching layer
- [x] Kubernetes deployment manifests
- [x] Terraform AWS infrastructure

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

### Let's Connect

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/abdallah-safi)
[![Email](https://img.shields.io/badge/Email-Contact-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:abdullahsf2001@gmail.com)

---

**Built to learn microservices, distributed transactions, and cloud deployment**

</div>