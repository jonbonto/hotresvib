-- V10__phase8_email_notifications.sql
-- Email notification system tables for Phase 8

-- Email logs table for tracking all email sends
CREATE TABLE IF NOT EXISTS email_logs (
    id VARCHAR(36) PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_logs_status_check CHECK (status IN ('SUCCESS', 'FAILURE'))
);

-- Create indexes for email_logs
CREATE INDEX idx_email_logs_recipient ON email_logs(recipient_email);
CREATE INDEX idx_email_logs_sent_at ON email_logs(sent_at);
CREATE INDEX idx_email_logs_status ON email_logs(status);
CREATE INDEX idx_email_logs_template ON email_logs(template_name);

-- Email unsubscribe table for managing opt-outs
CREATE TABLE IF NOT EXISTS email_unsubscribes (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    reason TEXT,
    unsubscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    email_type VARCHAR(50) NOT NULL DEFAULT 'ALL',
    CONSTRAINT email_unsubscribes_type_check CHECK (email_type IN ('ALL', 'PROMOTIONAL', 'NOTIFICATIONS'))
);

-- Create indexes for email_unsubscribes
CREATE INDEX idx_unsubscribe_email ON email_unsubscribes(email);
CREATE INDEX idx_unsubscribe_date ON email_unsubscribes(unsubscribed_at);

-- Add comments for documentation
COMMENT ON TABLE email_logs IS 'Audit log for all email send attempts';
COMMENT ON TABLE email_unsubscribes IS 'Email unsubscribe preferences for guests';
COMMENT ON COLUMN email_logs.status IS 'Email send status: SUCCESS or FAILURE';
COMMENT ON COLUMN email_unsubscribes.email_type IS 'Type of emails to unsubscribe from: ALL, PROMOTIONAL, or NOTIFICATIONS';
