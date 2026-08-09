# ClearLedger

**A distributed financial operating system for freelancers and multi-income earners.**

ClearLedger helps users with multiple income streams — salary, freelancing, investments — track income, log expenses against budgets, manage debt payoff strategies, and monitor net worth over time, all in one unified dashboard.

Built as a production-grade microservices system to demonstrate backend engineering patterns used at fintech companies.

---

## Live Demo

> **Frontend:** [https://clearledger.vercel.app](https://clearledger.vercel.app)  
> **API Gateway:** [https://clearledger-gateway.railway.app](https://clearledger-gateway.railway.app)

**Demo credentials:**  
Email: `demo@clearledger.com` | Password: `demo1234`

---

## Architecture

```
React Frontend (Vercel)
        │
        │ HTTPS
        ▼
┌─────────────────────────────────────────────┐
│              API Gateway  :8080             │
│   JWT validation · Rate limiting · Routing  │
└──────┬──────┬──────┬──────┬─────────────────┘
       │      │      │      │
       ▼      ▼      ▼      ▼
   User   Income  Expense  Debt    NetWorth
  :8081   :8082   :8083   :8084    :8085
    │       │       │       │         │
  PG:     PG:     PG:     PG:   PG: + Redis
 users   income expense  debt   networth
                  │                  ▲
                  └── Kafka ─────────┘
                  expense.logged event
```

### Communication patterns

| Flow | Type | Why |
|---|---|---|
| All clients → Gateway | HTTPS | Single entry point, JWT validated at the edge |
| Gateway → Services | HTTP (Docker DNS) | Synchronous routing, no Eureka needed |
| Expense → Net Worth | Kafka async | Fire-and-forget — expense response is not blocked by net worth recalculation |
| Net Worth → Debt | REST (RestTemplate) | Synchronous — net worth calculation requires the exact current liability total |
| Net Worth → Redis | Read/Write | CQRS — pre-computed summary served from cache on every dashboard load |

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Security | Spring Security 6 + JWT (JJWT) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 (one per service) |
| Messaging | Apache Kafka |
| Cache | Redis 7 |
| Containerization | Docker + Docker Compose |

### Frontend
| Layer | Technology |
|---|---|
| Framework | React 18 + Vite |
| Routing | React Router v6 |
| HTTP | Axios (with token refresh interceptor) |
| Charts | Recharts |
| Styling | Tailwind CSS |

---

## Services

### API Gateway `:8080`
Single entry point for all client requests. Validates JWT before forwarding to any downstream service. Extracts `userId` from the token and injects it as `X-User-Id` header — downstream services never touch JWT directly.

**Key patterns:** Global JWT filter, Redis-backed rate limiter, CORS at WebFlux level.

### User Service `:8081`
Handles authentication only. Issues short-lived JWT access tokens (15 min) and long-lived refresh tokens (7 days). Refresh tokens are stored in PostgreSQL and rotated on every use — the old token is immediately invalidated to prevent replay attacks.

**Key patterns:** BCrypt password hashing, refresh token rotation, stateless JWT.

### Income Service `:8082`
Two-level hierarchy: income **streams** (e.g. "Freelancing", "Salary") and individual **entries** under each stream. Monthly aggregation via JPQL `GROUP BY` queries.

**Key patterns:** Spring Data JPA aggregations, DTO projection, pagination.

### Expense Service `:8083`
Logs expenses and enforces monthly category budgets. Every new expense triggers a Kafka event to Net Worth Service for asynchronous recalculation.

**Key patterns:** Kafka producer, budget breach tracking, `EXTRACT` date functions in JPQL.

### Debt Service `:8084`
Manages debts and generates optimised payoff plans. Snowball and Avalanche strategies are implemented as interchangeable Java classes behind a common interface.

**Key patterns:** Strategy Pattern, amortisation simulation, internal REST endpoint consumed by Net Worth.

### Net Worth Service `:8085`
The aggregator. Combines assets (owned) and liabilities (owed) into a net worth snapshot. Implements CQRS — the write side recalculates on Kafka events, the read side always serves from Redis.

**Key patterns:** CQRS, Kafka consumer, Redis caching with TTL + explicit invalidation, inter-service REST call.

---

## Key Engineering Decisions

### Why database-per-service?
Each service owns its data exclusively. Net Worth Service cannot `JOIN` against the Debt Service database — it must call the Debt Service API. This ensures independent deployability: a schema change in one service cannot break another.

### Why Kafka instead of direct REST for Expense → Net Worth?
Expense Service's response time should not depend on how long Net Worth takes to recalculate. With Kafka, Expense publishes an event and returns `201` immediately. Net Worth reacts asynchronously. If Net Worth goes down, the event is retained in Kafka and processed on recovery. This is **eventual consistency** — acceptable for a dashboard value that updates every few seconds.

### Why is the refresh token a UUID and not a JWT?
A JWT is stateless — once issued, it cannot be revoked without a blocklist. A UUID stored in PostgreSQL can be deleted or flagged `revoked=true` instantly. For a financial app where session revocation must be immediate (e.g. on suspicious activity), a database-backed refresh token is the correct choice.

### Why CQRS in Net Worth Service?
The summary endpoint is read-heavy — every dashboard load hits it. Computing it live requires a cross-service REST call to Debt + aggregating all assets. Under load, this is expensive. CQRS separates the write path (triggered by Kafka events, runs async) from the read path (always hits Redis, sub-millisecond). The dashboard loads instantly regardless of calculation complexity.

### Why no Eureka?
Docker Compose assigns each container a DNS name matching its service name (`debt-service`, `redis`, etc.). Services call each other via these names directly. Eureka adds value when containers scale dynamically across multiple hosts — unnecessary for this architecture where Docker handles DNS.

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker Desktop

### Start infrastructure + all services

```bash
git clone https://github.com/Bhagyesh2003/clearledger.git
cd clearledger
docker compose up --build
```

This builds all 6 service images and starts 13 containers:
- API Gateway, User, Income, Expense, Debt, Net Worth services
- 5 PostgreSQL databases (one per service)
- Redis, Kafka, Zookeeper

### Start frontend

```bash
git clone https://github.com/Bhagyesh2003/clearledger-frontend.git
cd clearledger-frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

### Running services individually (development)

Each service can be run standalone in IntelliJ while the infrastructure runs in Docker:

```bash
# Start infrastructure only
docker compose up -d zookeeper kafka redis users-db income-db expense-db debt-db networth-db

# Run any service from its directory
cd user-service && mvn spring-boot:run
```

---

## API Reference

### Authentication
```
POST /api/auth/register     Register new user
POST /api/auth/login        Login, returns JWT + refresh token
POST /api/auth/refresh      Rotate refresh token, get new access token
```

### Income
```
POST /api/income/streams              Create income stream
GET  /api/income/streams              List all streams
POST /api/income/entries              Log income entry
GET  /api/income/entries?streamId=    List entries (optional stream filter)
GET  /api/income/summary?month=&year= Monthly income by stream
```

### Expenses
```
POST /api/expenses                   Log expense
GET  /api/expenses?category=         List expenses (optional category filter)
POST /api/budgets                    Set monthly budget per category
GET  /api/budgets?month=&year=       Budgets with % used
```

### Debts
```
POST /api/debts                                   Add debt
GET  /api/debts                                   List active debts
GET  /api/debts/payoff-plan?strategy=&budget=     Snowball or avalanche plan
POST /api/debts/{id}/payment                      Record payment
```

### Net Worth
```
POST /api/assets                 Add asset
GET  /api/assets                 List assets
GET  /api/networth/summary       Current net worth (from Redis cache)
GET  /api/networth/history       All snapshots for trend chart
POST /api/networth/recalculate   Force recalculation (dev/testing)
```

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>` header.

---

## Project Structure

```
clearledger/
├── api-gateway/
├── user-service/
├── income-service/
├── expense-service/
├── debt-service/
├── networth-service/
└── docker-compose.yml

clearledger-frontend/
├── src/
│   ├── api/          # Service client wrappers
│   ├── components/   # Layout, StatCard, ProtectedRoute
│   ├── context/      # AuthContext (JWT + refresh)
│   └── pages/        # Dashboard, NetWorth, Income, Expenses, Debts
└── vite.config.js
```

---

## Planned Enhancements

- **Tax estimation** — Indian income tax calculation (old/new regime) with deduction tracking (80C, 80D)
- **Notification service** — Budget breach alerts and quarterly advance tax reminders via Kafka + email
- **Bank statement parser** — PDF upload → auto-parse transactions into expense/income entries
- **Resilience4j circuit breakers** — Fault tolerance on the Net Worth → Debt synchronous call
- **Distributed tracing** — Zipkin + Micrometer Tracing for request tracing across all 5 services

---

## Author

**Bhagyesh Chaudhari**  
Full Stack Developer · Deloitte USI Mumbai  
[LinkedIn](https://linkedin.com/in/bhagyesh-chaudhari) · [GitHub](https://github.com/Bhagyesh2003)
