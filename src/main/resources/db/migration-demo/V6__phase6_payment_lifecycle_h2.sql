-- Phase 6 (H2): Payment Integration & Reservation Lifecycle (H2-safe)

-- Add new columns to payments table (H2 supports IF NOT EXISTS)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS metadata CLOB;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

-- Add unique constraint on idempotency_key if not exists (H2 doesn't support IF NOT EXISTS for constraints)
-- Use CREATE INDEX to emulate uniqueness checking for demo purposes
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_idempotency_key_demo ON payments(idempotency_key);

-- Index on payment_intent_id
CREATE INDEX IF NOT EXISTS idx_payments_payment_intent_id_demo ON payments(payment_intent_id);

-- Update existing PENDING reservations to DRAFT (safer default)
UPDATE reservations SET status = 'DRAFT' WHERE status = 'PENDING';
