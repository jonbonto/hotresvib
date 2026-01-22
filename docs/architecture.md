# 🏨 Hotel Reservation System — Architecture

> **Single Source of Truth** for all Copilot Agent sessions.
> Every change, PR, and phase **MUST** comply with this document.

---

## 1. Purpose & Principles

This document defines the **canonical architecture** of the Hotel Reservation System.
It exists to:

* Prevent architectural drift across autonomous Copilot Agent sessions
* Enforce consistent domain boundaries
* Enable safe, phased development with 1 PR per session

### Core Principles

* Clean Architecture
* SOLID principles
* Domain-Driven Design (DDD-inspired)
* Explicit state machines (no implicit behavior)
* Transaction safety over convenience
* Stateless services, stateful domain

---

## 2. High-Level System Architecture

### Components

```
[ Next.js App Router ]  --->  [ Spring Boot REST API ]  --->  [ PostgreSQL ]
         |                            |
         |                            ---> [ Payment Provider / Webhook ]
         |
         ---> [ CDN / Edge Cache ]
```

### Responsibilities

| Layer   | Responsibility                                  |
| ------- | ----------------------------------------------- |
| Next.js | UI, UX, routing, SSR/SEO, auth state            |
| API     | Business logic, validation, transactions        |
| DB      | Source of truth for availability & reservations |

---

## 3. Domain Boundaries (CRITICAL)

Each domain **must not leak** into others.

### 3.1 Reservation Domain

**Responsibility:**

* Owns booking intent
* Owns reservation lifecycle
* Guarantees no overbooking

**Key Concepts:**

* Reservation
* ReservationStatus
* ReservationPolicy

**States:**

```
DRAFT → PENDING_PAYMENT → CONFIRMED
                     → EXPIRED
CONFIRMED → CANCELLED → REFUNDED
```

---

### 3.2 Availability Domain

**Responsibility:**

* Source of truth for room availability
* Concurrency-safe inventory handling

**Rules:**

* Availability is date-based
* Availability is locked during reservation creation
* Availability is released on expiration or cancellation

**NEVER:**

* Compute availability on the frontend
* Trust cached availability without DB validation

---

### 3.3 Pricing Domain

**Responsibility:**

* Nightly rate calculation
* Seasonal / weekend / promo logic

**Rules:**

* Pricing is immutable once reservation is CONFIRMED
* Pricing breakdown is persisted in reservation

---

### 3.4 Payment Domain

**Responsibility:**

* Payment intent tracking
* Idempotent confirmation
* Refund lifecycle

**Payment States:**

```
PENDING → PAID → REFUNDED
       → FAILED
```

**Rules:**

* Payment success ≠ reservation confirmation until transaction completes
* Webhooks must be idempotent

---

### 3.5 User & Access Domain

**Roles:**

* CUSTOMER
* STAFF
* ADMIN

**Rules:**

* Authorization enforced in backend only
* Frontend guards are UX-only

---

## 4. Backend Architecture (Spring Boot + Kotlin)

### 4.1 Layering

```
controller/   → REST API, DTOs
application/  → use cases
 domain/      → entities, aggregates, policies
 infrastructure/ → DB, external services
```

### 4.2 Aggregates

| Aggregate Root | Owns                              |
| -------------- | --------------------------------- |
| Reservation    | ReservationItems, PricingSnapshot |
| RoomType       | Rooms                             |
| Payment        | PaymentAttempts                   |

---

## 5. Overbooking Prevention Strategy (MANDATORY)

### Strategy Used

* **Pessimistic locking** on availability rows
* Single DB transaction for:

  * availability lock
  * reservation creation

### Why

* Simpler correctness model
* Predictable under high concurrency

### Explicitly Rejected

* Optimistic locking only
* Frontend-based availability locking

---

## 6. API Standards

### REST Rules

* Resource-oriented endpoints
* No RPC-style naming

### Error Format

```json
{
  "code": "RESERVATION_CONFLICT",
  "message": "Room not available for selected dates"
}
```

### Status Codes

* 400 → Validation
* 401 → Unauthorized
* 403 → Forbidden
* 409 → Conflict (availability)

---

## 7. Frontend Architecture (Next.js App Router)

### Folder Structure

```
app/
  (public)/
  (auth)/
  (customer)/
  (admin)/
components/
lib/api/
lib/auth/
```

### Rules

* Server Components by default
* Client Components only when needed
* No business logic in UI components

---

## 8. State Ownership

| State              | Owner         |
| ------------------ | ------------- |
| Availability       | Backend       |
| Pricing            | Backend       |
| Reservation Status | Backend       |
| Auth Session       | Backend token |

---

## 9. Cross-Cutting Concerns

### Logging & Auditing

* Reservation status changes
* Payment state changes

### Timezone Handling

* Store all dates in UTC
* Convert only at UI boundary

---

## 10. Phase Gates

No phase may:

* Modify previous domain contracts
* Introduce cross-domain coupling
* Skip documentation update

Every phase **must**:

* Update this document if decisions change
* Pass architecture validation PR

---

## 11. Decision Log

| Date | Decision            | Reason             |
| ---- | ------------------- | ------------------ |
| TBD  | Pessimistic locking | Strong consistency |

---

## 12. Enforcement Rule

> If code conflicts with this document:
> **THE CODE IS WRONG, NOT THE DOCUMENT.**

All Copilot Agents must comply with this file before making changes.
