-- Flyway migration: Phase 4 schema additions
-- Adds password_hash to users, adjusts column types, creates payments & pricing_rules

-- Add password_hash column to users (V1 created users without it)
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
UPDATE users SET password_hash = '' WHERE password_hash IS NULL;
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;

-- V1 used base_rate; JPA entity expects base_rate_amount. Rename if needed.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rooms' AND column_name='base_rate')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rooms' AND column_name='base_rate_amount') THEN
    ALTER TABLE rooms RENAME COLUMN base_rate TO base_rate_amount;
  END IF;
END $$;

-- Widen precision
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rooms' AND column_name='base_rate_amount') THEN
    ALTER TABLE rooms ALTER COLUMN base_rate_amount TYPE NUMERIC(19,4);
  END IF;
END $$;

-- V1 used total_amount; JPA expects total_amount_amount.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='reservations' AND column_name='total_amount')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='reservations' AND column_name='total_amount_amount') THEN
    ALTER TABLE reservations RENAME COLUMN total_amount TO total_amount_amount;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='reservations' AND column_name='total_amount_amount') THEN
    ALTER TABLE reservations ALTER COLUMN total_amount_amount TYPE NUMERIC(19,4);
  END IF;
END $$;

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
  id UUID PRIMARY KEY,
  reservation_id UUID NOT NULL,
  amount_amount NUMERIC(19,4) NOT NULL,
  amount_currency VARCHAR(3) NOT NULL,
  status VARCHAR(50) NOT NULL,
  payment_method VARCHAR(100) NOT NULL,
  transaction_id VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- V1 used 'amount'; JPA expects 'amount_amount'. Rename if needed.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payments' AND column_name='amount')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payments' AND column_name='amount_amount') THEN
    ALTER TABLE payments RENAME COLUMN amount TO amount_amount;
  END IF;
END $$;

-- V1 created payments.id as TEXT; JPA entity expects UUID. Convert if needed.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payments' AND column_name='id' AND data_type='text') THEN
    ALTER TABLE payments ALTER COLUMN id TYPE UUID USING id::uuid;
  END IF;
END $$;

-- Add payment_method column (required by JPA entity, not created by V1)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_method VARCHAR(100) NOT NULL DEFAULT 'unknown';
-- Add transaction_id column (nullable, not created by V1)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(255);

-- Create pricing_rules table
CREATE TABLE IF NOT EXISTS pricing_rules (
  id VARCHAR(255) PRIMARY KEY,
  room_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  price_amount NUMERIC(19,4) NOT NULL,
  price_currency VARCHAR(3) NOT NULL,
  description VARCHAR(1024),
  CONSTRAINT fk_pricing_rule_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- V1 used 'price'; JPA expects 'price_amount'. Rename if needed.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='pricing_rules' AND column_name='price')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='pricing_rules' AND column_name='price_amount') THEN
    ALTER TABLE pricing_rules RENAME COLUMN price TO price_amount;
  END IF;
END $$;

-- Indexes (IF NOT EXISTS for safety)
CREATE INDEX IF NOT EXISTS idx_reservations_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_reservation ON payments(reservation_id);
