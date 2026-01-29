-- Add is_featured column to hotels (Postgres)
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS is_featured boolean NOT NULL DEFAULT false;
