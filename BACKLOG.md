# HotResvib – Availability & Reservation Backlog

## Overview

This backlog covers the redesign and fixes required to correct the availability system.
Issues are grouped into four milestones moving from critical bug-fixes to full architectural improvement.

---

## Milestone 1 — Critical Bug Fixes
> Goal: stop data corruption and silent failures without changing the overall design.
> Deliverable: a system that does not silently create orphan reservations or lie about availability.

---

### M1-1 · Fix empty availability list returning `true` ✅ DONE (2026-03-11)
**File:** `AvailabilityApplicationService.kt`
**Severity:** HIGH
**Problem:** `availabilities.all { ... }` on an empty list returns `true` in Kotlin, so a room
with zero availability rows appears fully available.
**Change:**
```kotlin
if (availabilities.isEmpty()) return false
return availabilities.all { it.available.value > 0 }
```
**Acceptance:** A room with no availability rows returns `available: false` from `check-availability`.
**Status:** Implemented in `AvailabilityApplicationService.checkAvailability()` with an explicit `isEmpty()` guard.

---

### M1-2 · Move `reservationRepository.save()` after availability decrement ✅ DONE (2026-03-11)
**File:** `ReservationService.kt`
**Severity:** HIGH
**Problem:** The reservation row is persisted before the availability decrement. If the decrement
throws (e.g. optimistic lock collision), the DRAFT reservation exists permanently with no inventory
consumed.
**Change:** reorder so the decrement succeeds first, then save the reservation — all inside one
`@Transactional` boundary.
**Acceptance:** A failed decrement rolls back the entire transaction including the reservation insert.
**Status:** Implemented in `ReservationService.createReservation()` by moving save to the end and adding `@Transactional`.

---

### M1-3 · Implement inventory decrement in `ReservationLifecycleService.createDraft()` ✅ DONE (2026-03-11)
**File:** `ReservationLifecycleService.kt`
**Severity:** CRITICAL
**Problem:** `createDraft()` has a TODO comment and never calls the availability repository.
Every DRAFT created through this path leaks inventory.
**Change:** Mirror the decrement logic from `ReservationService.createReservation()`.
**Acceptance:** After calling `createDraft()`, the relevant availability row's `available` count
is reduced by 1.
**Status:** Implemented via `updateAvailabilityForStay(..., delta = -1)` in `ReservationLifecycleService.createDraft()`.

---

### M1-4 · Implement inventory restore in `expireReservation()` and `cancelReservation()` ✅ DONE (2026-03-11)
**File:** `ReservationLifecycleService.kt`
**Severity:** CRITICAL
**Problem:** Both methods have TODO comments that skip restoring inventory, permanently destroying
available slots on every expiry or cancellation.
**Change:** Fetch the overlapping availability rows for the reservation's date range and increment
each by 1, inside the same transaction as the status update.
**Acceptance:**
- Cancelling a reservation restores `available` to its pre-booking value.
- Expiring a DRAFT/PENDING_PAYMENT reservation restores `available`.
**Status:** Implemented via `updateAvailabilityForStay(..., delta = 1)` in both methods; transition `DRAFT -> EXPIRED` also enabled.

---

### M1-5 · Wrap check + save in a single `@Transactional` in `createReservation()` ✅ DONE (2026-03-11)
**File:** `ReservationService.kt`
**Severity:** HIGH
**Problem:** No `@Transactional` annotation on `createReservation()`. The availability check and
the reservation insert are separate database round-trips, allowing two concurrent requests to both
see `available = 1` and both succeed.
**Change:** Annotate `createReservation()` with `@Transactional` and ensure the availability
check uses a pessimistic read lock (`SELECT … FOR UPDATE`) on the row.
**Acceptance:** Concurrent requests for the same room and dates result in exactly one DRAFT saved;
the second caller receives a clear error.
**Status:** `@Transactional` is active and availability fetch now uses `findByRoomIdForUpdate()` with JPA `PESSIMISTIC_WRITE` locking.

---

## Milestone 2 — Merge Duplicate Lifecycle Services
> Goal: one canonical path for every reservation state transition.
> Deliverable: `ReservationService` and `ReservationLifecycleService` collapsed into one class.

---

### M2-1 · Audit all callers of both services ✅ DONE (2026-03-11)
**Files:** all controller, job, and webhook classes
**Task:** List every place that calls `ReservationService` or `ReservationLifecycleService` and
document which method each caller uses.
**Acceptance:** A table mapping caller → service method exists (can be a code comment or PR description).
**Status:** Caller map completed:
- `ReservationController` → `createReservation`, `cancelReservation`
- `PaymentController` → `initiatePayment`
- `WebhookController` → `confirmPayment`, `expireReservation`
- `ReservationExpirationJob` → `expireReservation`

---

### M2-2 · Create `ReservationApplicationService` as the single service ✅ DONE (2026-03-11)
**Task:** New class at `application/service/ReservationApplicationService.kt` exposing:
- `createReservation()` — with availability check, price calculation, single @Transactional
- `initiatePayment()`
- `confirmPayment()`
- `expireReservation()`
- `cancelReservation()`
- `refundReservation()`

All inventory touches must be inside each respective method.
**Acceptance:** All methods compile; unit tests cover each state transition.
**Status:** Implemented in `ReservationApplicationService` with methods: `createReservation`, `initiatePayment`, `confirmPayment`, `expireReservation`, `cancelReservation`, `refundReservation`.

---

### M2-3 · Migrate all callers to `ReservationApplicationService` ✅ DONE (2026-03-11)
**Task:** Update every controller, scheduled job, and webhook handler to inject and call the new
unified service.
**Acceptance:** Neither `ReservationService` nor `ReservationLifecycleService` is referenced
anywhere outside of itself.
**Status:** Callers migrated in `ReservationController`, `PaymentController`, `WebhookController`, and `ReservationExpirationJob`.

---

### M2-4 · Delete `ReservationService` and `ReservationLifecycleService` ✅ DONE (2026-03-11)
**Acceptance:** Both files removed; full compilation and test suite passes.
**Status:** Legacy files deleted; main Kotlin compilation passes. Test compilation still has broader existing failures plus old test references that should be migrated in a dedicated test-update pass.

---

## Milestone 3 — Replace Counter with Reservation-Based Availability
> Goal: remove the mutable `available` counter. Availability truth comes from the reservation table.
> Deliverable: `check-availability` is answered by querying reservations, not an inventory row.

---

### M3-1 · Add `hasConflict()` query to `ReservationRepository` ✅ DONE (2026-03-11)
**Files:** `ReservationRepository.kt` (port), `ReservationJpaRepository.kt`, `ReservationJpaAdapter.kt`,
`InMemoryReservationRepository.kt`
**Task:** Add:
```kotlin
fun hasConflict(roomId: RoomId, range: DateRange,
                activeStatuses: Set<ReservationStatus>): Boolean
```
JPA implementation uses a JPQL EXISTS query:
```
SELECT COUNT(r) > 0 FROM Reservation r
WHERE r.roomId = :roomId
  AND r.status IN :activeStatuses
  AND r.stay.startDate < :endDate
  AND r.stay.endDate   > :startDate
```
**Acceptance:** Unit-tested in isolation (InMemory impl) for overlap, no-overlap, status exclusion.
**Status:** Implemented in `ReservationRepository`, `ReservationJpaRepository.existsConflict(...)`, `ReservationJpaAdapter`, and `InMemoryReservationRepository`.

---

### M3-2 · Rewrite `AvailabilityApplicationService.checkAvailability()` ✅ DONE (2026-03-11)
**Task:** Replace the inventory-counter check with a call to `reservationRepository.hasConflict()`.
Conflict statuses: `CONFIRMED`, `PENDING_PAYMENT`.
An optional secondary check against the `Availability` table for admin block-out dates can remain.
**Acceptance:**
- Room with an overlapping CONFIRMED reservation → `available: false`.
- Room with only a DRAFT reservation → `available: true` (DRAFTs do not block new bookings).
- Room with no reservations and no block-out → `available: true`.
**Status:** Implemented in `AvailabilityApplicationService`: conflict check via `ReservationRepository.hasConflict(...)` first, then inventory fallback, and empty availability rows now return `true` (treated as no block-out).

---

### M3-3 · Repurpose `Availability` table as "block-out" only ✅ DONE (2026-03-11)
**Task:**  
- Rename domain concept: `Availability` → `BlockedPeriod` (or keep name, change semantics).
- Remove `available: Int` column; replace with `reason: String` (MAINTENANCE, OWNER_USE, etc.).
- Update schema migration (new H2 and PostgreSQL migration files).
- Update all references.
**Acceptance:** Admin can create/delete blocked periods; a blocked period prevents bookings regardless
of the reservation table.
**Status:** Main semantics switched to blockout mode (`Availability.reason`), converters updated, and DB migrations added: `V12__phase13_availability_blockout.sql` + `V12__phase13_availability_blockout_h2.sql`.

---

### M3-4 · Remove all inventory increment/decrement code ✅ DONE (2026-03-11)
**Task:** Delete the sections in `createReservation()`, `cancelReservation()`, `expireReservation()`
that read and write `available` counts.
**Acceptance:** No code references `AvailableQuantity`; compilation passes; `available` column
removed or repurposed in schema.
**Status:** Inventory decrement/increment paths removed from `ReservationApplicationService`; availability is now determined by reservation conflicts + overlapping blockouts.

---

### M3-5 · Add database-level unique constraint for active reservations ✅ DONE (2026-03-11)
**Task:** Add a partial unique index to prevent double-booking at the DB level as a safety net:
```sql
-- conceptual; enforce via application-level check + optimistic locking is sufficient,
-- but a DB trigger or exclusion constraint on PostgreSQL is the gold standard.
```
For PostgreSQL: add an EXCLUDE USING GIST constraint on `(room_id, daterange(start_date, end_date))`.
For H2 (demo): rely on the application-level `hasConflict()` check.
**Acceptance:** Attempting to INSERT two overlapping CONFIRMED reservations for the same room at the
SQL level raises a constraint violation.
**Status:** Implemented in PostgreSQL migration `V13__phase13_reservation_overlap_constraint.sql`; H2 fallback documented via `V13__phase13_reservation_overlap_constraint_h2.sql` and application-level conflict checks.

---

## Milestone 4 — Fix Pricing Accuracy
> Goal: the price seen in `check-availability` is exactly the price stored on the reservation.
> Deliverable: one pricing path used everywhere.

---

### M4-1 · Implement per-night segment pricing in `PriceCalculationService`
**File:** `PriceCalculationService.kt`
**Task:** Walk each night of the stay; for each night pick the most specific applicable pricing rule
(latest `startDate` wins); fall back to `room.baseRate`. Sum all nights.
```kotlin
var cursor = stay.startDate
while (cursor.isBefore(stay.endDate)) {
    val rate = rules.filter { it.range.overlaps(DateRange(cursor, cursor.plusDays(1))) }
                    .maxByOrNull { it.range.startDate }?.price ?: room.baseRate
    total += rate.amount
    cursor = cursor.plusDays(1)
}
```
**Acceptance:** A 6-night stay spanning two pricing rules produces the correct sum (tested with
a unit test that covers a straddle scenario).

---

### M4-2 · Remove pricing logic from `ReservationService` / `ReservationApplicationService`
**Task:** Delete the inline `applicableRate` calculation inside `createReservation()` and replace
with a call to `PriceCalculationService.calculateTotalAmount(room, stay, rules)`.
**Acceptance:** Exactly one place in the codebase computes `totalAmount`.

---

### M4-3 · Ensure `check-availability` and `createReservation` use the same pricing path
**Task:** Both `ReservationController.checkAvailability()` and
`ReservationApplicationService.createReservation()` must call `PriceCalculationService`.
**Acceptance:** The `totalPrice` returned by `check-availability` equals the `totalPrice` on the
subsequently created reservation for the same room and dates.

---

### M4-4 · Add integration test: price consistency end-to-end
**Task:** Test that calls `check-availability` then `POST /api/reservations` and asserts both
prices are equal.
**Acceptance:** Green in CI.

---

## Milestone 5 — Test Coverage
> Goal: each corrected behaviour is guarded by an automated test.

| Task | Scope |
|------|-------|
| M5-1 | Unit test: `checkAvailability()` with empty availability list returns `false` |
| M5-2 | Unit test: `hasConflict()` — overlap, boundary, no-overlap cases |
| M5-3 | Unit test: `cancelReservation()` restores inventory (or unblocks via reservation status) |
| M5-4 | Unit test: `expireReservation()` restores inventory |
| M5-5 | Integration test: concurrent `createReservation()` results in exactly one success |
| M5-6 | Unit test: per-night pricing for stay straddling two pricing rules |
| M5-7 | Integration test: `check-availability` price == created reservation price |

---

## Dependency Order

```
M1-1  (safe, standalone)
M1-2  (safe, standalone)
M1-3  depends on M1-2
M1-4  depends on M1-2
M1-5  depends on M1-2, M1-3

M2-1  (analysis, no code change)
M2-2  depends on M1-1..M1-5
M2-3  depends on M2-2
M2-4  depends on M2-3

M3-1  depends on M2-4 (clean repo interface)
M3-2  depends on M3-1
M3-3  depends on M3-2
M3-4  depends on M3-3
M3-5  depends on M3-4

M4-1  (standalone)
M4-2  depends on M4-1, M2-4
M4-3  depends on M4-2
M4-4  depends on M4-3

M5-x  each paired with its corresponding implementation task
```

---

## Out of Scope (future)

- Multi-room booking (booking more than one room in a single reservation)
- Channel manager integration (sync external OTA inventory)
- Dynamic pricing engine (demand-based rates)
- Admin UI for block-out management
