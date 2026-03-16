# ADR-001: Concurrency Control Strategy

## Status
Accepted

## Date
2025-07-22

## Context
The hotel reservation system handles concurrent access from multiple users booking rooms, processing payments, and managing availability. Race conditions could lead to double-bookings, incorrect availability, or payment processing errors.

## Decision
We adopt a **layered concurrency control strategy** combining optimistic locking, pessimistic locking, and database-level constraints.

### Layer 1: JPA Optimistic Locking (`@Version`)
Applied to frequently-updated, conflict-sensitive entities:
- **Reservation** — status transitions (DRAFT → PENDING_PAYMENT → CONFIRMED → CANCELLED)
- **Availability** — quantity adjustments during booking
- **Payment** — status transitions during payment lifecycle
- **User** — profile updates, login attempt counters
- **Hotel** — metadata updates

**Not applied to:**
- **Room, PricingRule** — Low update frequency; admin-only writes. Added version fields are deferred until multi-admin support is needed.
- **AuditLog, EmailLog** — Append-only; no concurrent updates.
- **Review** — One review per user per hotel (unique constraint suffices).

### Layer 2: Pessimistic Locking (SELECT FOR UPDATE)
Used in `AvailabilityJpaRepository.findByRoomIdAndIdIsNotNull()` to serialize availability checks during the reservation creation critical path, preventing phantom reads.

### Layer 3: Database Constraints (Safety Net)
- **EXCLUDE USING gist** on `availability` — prevents overlapping date ranges per room
- **EXCLUDE USING gist** on `reservations` — prevents overlapping CONFIRMED/PENDING_PAYMENT reservations per room
- **UNIQUE** on `payments.idempotency_key` — prevents duplicate payment processing
- **UNIQUE** on `reviews(user_id, hotel_id)` — one review per user per hotel

### Transaction Isolation
All transactions use PostgreSQL default **READ COMMITTED**. The combination of optimistic locking + database constraints is sufficient for this workload without escalating to SERIALIZABLE.

### Conflict Resolution
- Optimistic lock violations (`OptimisticLockingFailureException`) → HTTP 409 Conflict
- Database constraint violations (EXCLUDE/UNIQUE) → HTTP 409 Conflict with descriptive message
- Clients should retry on 409 with exponential backoff

## Consequences

### Positive
- Double-booking is impossible at the database level (EXCLUDE constraints)
- Concurrent admin edits surface cleanly via optimistic locking
- Payment idempotency prevents duplicate charges
- No global locks that would serialize all requests

### Negative
- TOCTOU gap in reservation creation (application check → save) is caught by DB constraint rather than application logic, resulting in less user-friendly error messages
- Room/PricingRule lack version fields, so concurrent admin updates may silently overwrite each other (acceptable given single-admin current usage)

### Risks
- If the system scales to multiple admin users editing rooms/pricing simultaneously, add `@Version` to Room and PricingRule entities
- High-contention scenarios (flash sales) may cause optimistic lock retries; consider pessimistic locking for those paths

## Related
- V1_init.sql: availability EXCLUDE constraint
- V8_phase11_security.sql: version columns on 5 entities
- V13_reservation_overlap_constraint.sql: reservation EXCLUDE constraint
