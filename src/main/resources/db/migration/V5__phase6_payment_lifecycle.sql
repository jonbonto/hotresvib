-- Phase 6: Payment Integration & Reservation Lifecycle
-- Add new reservation states and payment fields

-- Add new columns to payments table
ALTER TABLE payments
ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS metadata TEXT,
ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255) UNIQUE;

-- Add index on idempotency_key for fast webhook deduplication
CREATE INDEX IF NOT EXISTS idx_payments_idempotency_key ON payments(idempotency_key);

-- Add index on payment_intent_id for Stripe integration
CREATE INDEX IF NOT EXISTS idx_payments_payment_intent_id ON payments(payment_intent_id);

-- Note: ReservationStatus enum values handled by JPA @Enumerated(EnumType.STRING)
-- New states: DRAFT, PENDING_PAYMENT, CONFIRMED, CANCELLED, EXPIRED, REFUNDED
-- Old PENDING state can be migrated to DRAFT or PENDING_PAYMENT as needed

-- Update existing PENDING reservations to DRAFT (safer default)
UPDATE reservations SET status = 'DRAFT' WHERE status = 'PENDING';
