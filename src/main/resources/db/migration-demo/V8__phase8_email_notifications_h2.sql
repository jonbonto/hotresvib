-- V8__phase8_email_notifications_h2.sql
-- Email notification system tables for Phase 8 (H2 in-memory database)

CREATE TABLE IF NOT EXISTS email_logs (
    id VARCHAR(36) PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    error_message CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (status IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_email_logs_recipient ON email_logs(recipient_email);
CREATE INDEX idx_email_logs_sent_at ON email_logs(sent_at);
CREATE INDEX idx_email_logs_status ON email_logs(status);
CREATE INDEX idx_email_logs_template ON email_logs(template_name);

CREATE TABLE IF NOT EXISTS email_unsubscribes (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    reason CLOB,
    unsubscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    email_type VARCHAR(50) NOT NULL DEFAULT 'ALL',
    CHECK (email_type IN ('ALL', 'PROMOTIONAL', 'NOTIFICATIONS'))
);

CREATE INDEX idx_unsubscribe_email ON email_unsubscribes(email);
CREATE INDEX idx_unsubscribe_date ON email_unsubscribes(unsubscribed_at);
