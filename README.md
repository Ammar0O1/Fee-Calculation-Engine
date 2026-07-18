# Fee Calculation Engine

A configurable fee calculation engine for financial transactions, built in Java with Spring Boot. The engine resolves the most specific applicable fee rule for each side of a transaction (sender and receiver), calculates fees using pluggable strategies, applies rounding and caps, persists results, and exposes everything through a REST API.

This project was built by a team of three as a structured training program across three assignments: a pure Java engine, a database persistence layer, and a REST API.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.0
- **Persistence:** Spring Data JPA, Hibernate 6.6.15
- **Database:** PostgreSQL 16
- **Migrations:** Flyway
- **Build:** Maven
- **Utilities:** Lombok, Vavr
- **Testing:** JUnit 5, AssertJ, MockMvc

## Architecture

The codebase follows a layered architecture with dependencies pointing inward (outer layers depend on inner layers, never the reverse):

```
api            → controllers, DTOs, error handling (the web layer)
application    → use-case services
domain         → core business logic: rules, strategies, calculation (depends on nothing)
infrastructure → persistence: entities, repositories, mappers, migrations
```

The domain layer is framework-free and has no knowledge of the database or the web. Persistence and API concerns live in the outer layers and map to/from domain objects.

### Request flow

```
HTTP request → Controller → Service → Repository → PostgreSQL
                                 ↓
                          Domain engine
```

## Core Domain Concepts

- **FeeRule**: a rule defining how a fee is calculated, scoped by optional dimensions (user, user type, transaction type, currencies). Null dimensions act as wildcards.
- **FeeRuleResolver**: selects the most specific matching rule for each transaction side using a priority-scored, wildcard-aware match.
- **FeeSideDefinition**: how one side's fee is computed (mode, flat amount, percentage, tier brackets, caps).
- **CalculationMode**: FLAT, PERCENTAGE, HYBRID, TIERED_FLAT, TIERED_MARGINAL.
- **FeeApplier**: applies calculation, then rounding, then min cap, then max cap (order matters).
- **FeeCalculator**: orchestrates resolution, calculation, and result assembly for both sides.

Waived fees are represented as `null` (not zero); aggregation queries use `COALESCE` to treat them as zero when summing.

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose (for the database)

### Run the database

The PostgreSQL database runs in Docker Compose. From the project root:

```bash
docker compose up -d
```

This starts PostgreSQL 16 with the `fee_engine` database on `localhost:5432`. Flyway migrations run automatically on application startup.

### Run the application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

### Run the tests

Ensure the database is running first (`docker compose up -d`), then:

```bash
mvn test
```

## API Overview

Base path: `/api/fees`

### Rule Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/rules/defaults` | Create a default (wildcard) rule |
| POST | `/rules/user-types` | Create a user-type rule |
| POST | `/rules/users/{userId}` | Create a user-specific rule |
| GET | `/rules` | List rules |
| PUT | `/rules/{id}` | Update a rule |
| DELETE | `/rules/{id}` | Deactivate a rule |

### Calculation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/calculate` | Calculate fees for a transaction (persists a log) |
| POST | `/estimate` | Preview fees without persisting |

### History & Reporting

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/history` | Query past fee calculations with optional filters and pagination |
| GET | `/history/summary` | Aggregated fee totals (by transaction type, user type, currency) and averages |

**History filters** (all optional): `transactionId`, `userId`, `transactionType`, `sourceCurrency`, `destinationCurrency`, `dateFrom`, `dateTo`, plus `page` and `size` for pagination.

**Summary filters** (all optional): `dateFrom`, `dateTo`.

### Error Handling

All endpoints return a consistent error response format:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "No matching rule found for the transaction",
  "timestamp": "2026-07-14T11:30:00",
  "path": "/api/fees/calculate"
}
```

| Status | Meaning |
|--------|---------|
| 400 | Validation error (missing fields, invalid values) |
| 404 | Rule or resource not found |
| 409 | Conflict (duplicate rule) |
| 422 | Business rule violation (no matching rule) |

## Database

Schema is managed by Flyway migrations in `src/main/resources/db/migration`. Core tables:

- `fee_rule`: rule definitions
- `fee_side_definition`: per-side fee configuration
- `tier_bracket`: tier brackets for tiered calculation modes
- `fee_transaction_log`: record of every calculated fee

## Testing Notes

Integration tests run against the Docker Compose PostgreSQL instance (not an in-memory database), so the schema, migrations, and constraints tested match production behavior. Ensure `docker compose up -d` is running before executing the test suite.

## Team

Built by a team of three interns, with responsibilities rotating across assignments (schema/persistence, calculation engine, history /error handling).
