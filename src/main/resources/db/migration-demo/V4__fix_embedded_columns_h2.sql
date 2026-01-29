-- Fix embedded Money column names for H2 demo DB
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS amount NUMERIC(19,4);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS currency VARCHAR(3);
UPDATE reservations SET amount = total_amount_amount, currency = total_amount_currency WHERE total_amount_amount IS NOT NULL;
ALTER TABLE reservations DROP COLUMN IF EXISTS total_amount_amount;
ALTER TABLE reservations DROP COLUMN IF EXISTS total_amount_currency;

ALTER TABLE payments ADD COLUMN IF NOT EXISTS amount NUMERIC(19,4);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS currency VARCHAR(3);
UPDATE payments SET amount = amount_amount, currency = amount_currency WHERE amount_amount IS NOT NULL;
ALTER TABLE payments DROP COLUMN IF EXISTS amount_amount;
ALTER TABLE payments DROP COLUMN IF EXISTS amount_currency;
