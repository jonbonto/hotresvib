-- Phase 13: Convert availability from mutable inventory counters to blockout periods
-- Existing seed rows represented inventory counts; clear them before enabling blockout-only semantics.
DELETE FROM availability;

ALTER TABLE availability ADD COLUMN IF NOT EXISTS reason TEXT;
UPDATE availability SET reason = 'BLOCKED' WHERE reason IS NULL;
ALTER TABLE availability ALTER COLUMN reason SET NOT NULL;

ALTER TABLE availability DROP COLUMN IF EXISTS available;
