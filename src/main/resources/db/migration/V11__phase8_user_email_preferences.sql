-- V11__phase8_user_email_preferences.sql
-- Add unsubscribe token and marketing preference to users

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS unsubscribe_token VARCHAR(255) UNIQUE;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS marketing_opt_in BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX IF NOT EXISTS idx_users_unsubscribe_token ON users(unsubscribe_token);
COMMENT ON COLUMN users.unsubscribe_token IS 'One-time token used for unsubscribe links';
COMMENT ON COLUMN users.marketing_opt_in IS 'Whether user consents to marketing emails';
