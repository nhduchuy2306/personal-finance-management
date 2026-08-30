-- OCR Service Schema
-- Tables: receipts

CREATE TABLE receipts
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL,
    image_path     VARCHAR(500) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING',
    parsed_data    JSONB,
    confirmed_data JSONB,
    total_amount   BIGINT,
    receipt_date   DATE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP             DEFAULT NOW(),
    CONSTRAINT chk_receipt_status CHECK (status IN ('PROCESSING', 'PARSED', 'CONFIRMED', 'FAILED', 'DISCARDED'))
);

CREATE INDEX idx_receipts_user ON receipts (user_id);
CREATE INDEX idx_receipts_status ON receipts (user_id, status);
