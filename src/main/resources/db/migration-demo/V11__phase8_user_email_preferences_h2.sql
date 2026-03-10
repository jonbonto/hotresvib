-- V11__phase8_user_email_preferences_h2.sql
-- Add unsubscribe token and marketing preference to users (H2 in-memory database)

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS unsubscribe_token VARCHAR(255) UNIQUE;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS marketing_opt_in BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX IF NOT EXISTS idx_users_unsubscribe_token ON users(unsubscribe_token);
