# hotresvib

## Phase 0 — Architecture & Domain Modeling

### Scope
Phase 0 establishes the system boundaries, high-level architecture, and domain modeling for the Hotel Reservation System. It defines the core bounded contexts, communication flow, and API/error handling standards that the backend and frontend will implement in later phases.

### Decisions & Tradeoffs
- **Clean Architecture + DDD-inspired boundaries**: Keeps domain logic isolated from infrastructure, at the cost of extra layers and boilerplate.
- **REST over GraphQL**: Simpler integration for the initial phase; fewer moving parts for standard CRUD and booking workflows.
- **Server Components for Next.js**: Better performance and data-fetching ergonomics, but requires careful client/server separation for interactivity.
- **Single PostgreSQL database**: Simplicity and consistency; scaled read/write or sharding can be explored later if needed.

### System Architecture (Conceptual)
```
┌────────────────────┐        HTTPS/JSON         ┌──────────────────────────┐
│ Next.js Frontend   │  ───────────────────────> │ Spring Boot API (Kotlin) │
│ App Router + RSC   │                          │ Clean Architecture Layers │
└────────────────────┘                          └─────────────┬────────────┘
                                                             │
                                                     ┌───────▼────────┐
                                                     │ PostgreSQL DB  │
                                                     │ + Flyway       │
                                                     └────────────────┘
                                                             │
                                                     ┌───────▼────────┐
                                                     │ Payment Vendor │
                                                     │ (Webhook-ready)│
                                                     └────────────────┘
```

### Domain Boundaries (Bounded Contexts)
- **Reservation**: Booking lifecycle, policies, expiration, and state transitions.
- **Availability**: Inventory management, overbooking prevention, and date-range checks.
- **Pricing**: Base rates, seasonal rules, promotions, and totals.
- **Payment**: Payment intent, idempotency, webhook ingestion, and refunds.
- **User Management**: Accounts, roles, authentication, and authorization.

### Communication Flow (Next.js ↔ Spring Boot)
- **Request flow**: Next.js Server Components fetch data from `/api/v1/*` endpoints using typed DTOs.
- **Client-side interactions**: Client Components invoke REST endpoints via fetch for booking actions and authentication.
- **Backend response contracts**: JSON payloads with explicit resource naming and consistent pagination.

### Error Handling & API Standards
- **API versioning**: `/api/v1` base path for all endpoints.
- **Error format**: RFC 7807-compatible `application/problem+json` with fields:
  - `type`, `title`, `status`, `detail`, `instance`, and `errors` for field validation.
- **Validation**: Jakarta Validation annotations on inbound request DTOs.
- **Correlation**: `X-Request-Id` accepted and echoed for traceability.
- **Pagination**: `page`, `size`, `sort` query parameters with consistent response metadata.

### Summary (Phase 0)
- Defined the architecture shape, core bounded contexts, and interface standards.
- Established the communication flow between Next.js and Spring Boot.
- Chose API and error handling conventions to enforce consistency.

### Risks & Next Steps
- **Risks**: Overbooking edge cases require careful transactional design; payment webhooks add eventual consistency concerns.
- **Next steps**: Move to Phase 1 by implementing the core domain model, database schema, and persistence layer.

## Phase 1 — Core Domain Model & Persistence (Implemented)

Phase 1 delivers the foundational domain model (entities/value objects), repository ports, in-memory adapters, and initial Flyway schema. Persistence adapters are in-memory placeholders until database-backed repositories are introduced.
