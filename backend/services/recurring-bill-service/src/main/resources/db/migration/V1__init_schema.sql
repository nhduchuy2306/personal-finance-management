-- Recurring Bill Service Schema
-- Tables: recurring_bills, bill_payments

CREATE TABLE recurring_bills
(
    id                   UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id              UUID         NOT NULL,
    name                 VARCHAR(200) NOT NULL,
    category_id          UUID         NOT NULL,
    cycle_type           VARCHAR(20)  NOT NULL,
    cycle_value          INTEGER, -- for CUSTOM_DAYS: number of days
    estimated_amount     BIGINT       NOT NULL,
    is_fixed_amount      BOOLEAN      NOT NULL DEFAULT true,
    due_day_of_cycle     INTEGER, -- day of month for MONTHLY
    next_due_date        DATE         NOT NULL,
    reminder_days_before INTEGER      NOT NULL DEFAULT 3,
    is_active            BOOLEAN      NOT NULL DEFAULT true,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP             DEFAULT NOW(),
    CONSTRAINT chk_cycle_type CHECK (cycle_type IN ('MONTHLY', 'QUARTERLY', 'SEMI_ANNUAL', 'ANNUAL', 'CUSTOM_DAYS'))
);

CREATE TABLE bill_payments
(
    id                UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    recurring_bill_id UUID      NOT NULL REFERENCES recurring_bills (id),
    transaction_id    UUID,
    actual_amount     BIGINT    NOT NULL,
    payment_date      DATE      NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_recurring_bills_user ON recurring_bills (user_id);
CREATE INDEX idx_recurring_bills_next_due ON recurring_bills (next_due_date) WHERE is_active = true;
CREATE INDEX idx_bill_payments_bill ON bill_payments (recurring_bill_id);
