-- Notification Service Schema
-- Tables: notification_logs

CREATE TABLE notification_logs
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL,
    type          VARCHAR(50) NOT NULL,
    channel       VARCHAR(20) NOT NULL DEFAULT 'TELEGRAM',
    content       TEXT        NOT NULL,
    entity_id     VARCHAR(100), -- related entity (budget ID, bill ID, etc.)
    status        VARCHAR(30) NOT NULL DEFAULT 'SENT',
    error_message VARCHAR(500),
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notif_status CHECK (status IN ('SENT', 'FAILED', 'SKIPPED_NO_TELEGRAM'))
);

CREATE INDEX idx_notif_logs_user ON notification_logs (user_id);
CREATE INDEX idx_notif_logs_type ON notification_logs (user_id, type, created_at);
CREATE INDEX idx_notif_logs_entity ON notification_logs (entity_id) WHERE entity_id IS NOT NULL;
