-- System Config Table
-- Stores dynamic system-wide configuration as key-value pairs.
-- Shared across all services via common DB.

CREATE TABLE system_config
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    config_name VARCHAR(100) NOT NULL UNIQUE,
    value       VARCHAR(1000) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP             DEFAULT NOW()
);

CREATE INDEX idx_system_config_name ON system_config (config_name);
