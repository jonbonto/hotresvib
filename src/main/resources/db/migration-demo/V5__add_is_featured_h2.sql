-- Add is_featured column to hotels for H2 demo
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;
