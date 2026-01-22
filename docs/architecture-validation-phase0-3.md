# Architecture Validation — Phase 0–3 (Reservation Lifecycle)

Scope: Read-only validation between `/docs/architecture.md` and current codebase. No code changes were made.

| Mismatch | Architecture Expectation | Code Reality | Impact |
| --- | --- | --- | --- |
| Missing `DRAFT` state | Lifecycle starts at `DRAFT` before payment intent | `ReservationStatus` enum has no `DRAFT` | High |
| Missing `PENDING_PAYMENT` state | Transition `DRAFT → PENDING_PAYMENT → CONFIRMED` | Enum uses `PENDING` (no payment-specific state) | High |
| Missing `EXPIRED` state | `PENDING_PAYMENT` may go to `EXPIRED` | No `EXPIRED` status in enum or flows | High |
| Missing `REFUNDED` state | `CANCELLED → REFUNDED` supported | No `REFUNDED` status in enum or flows | High |
| Transition semantics | Payment-first flow required before confirmation | Creation sets status directly to `PENDING`; only `PENDING` can cancel | High |

Recommendation: Requires Design Gate decision before implementation changes.
