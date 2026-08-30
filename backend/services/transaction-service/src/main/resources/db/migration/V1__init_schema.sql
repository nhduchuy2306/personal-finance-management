-- Transaction Service Schema
-- Tables: transactions

CREATE TABLE transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    category_id         UUID NOT NULL,
    amount              BIGINT NOT NULL,
    type                VARCHAR(10) NOT NULL,  -- EXPENSE or INCOME
    note                VARCHAR(500),
    transaction_date    DATE NOT NULL,
    source              VARCHAR(20) NOT NULL DEFAULT 'MANUAL',  -- MANUAL, OCR, GROUP_SPLIT
    receipt_id          UUID,
    recurring_bill_id   UUID,
    group_expense_id    UUID,
    no_active_budget    BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_transaction_type CHECK (type IN ('EXPENSE', 'INCOME')),
    CONSTRAINT chk_transaction_source CHECK (source IN ('MANUAL', 'OCR', 'GROUP_SPLIT')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

-- Performance indexes
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_user_category_date ON transactions(user_id, category_id, transaction_date);
CREATE INDEX idx_transactions_user_type ON transactions(user_id, type);
CREATE INDEX idx_transactions_receipt ON transactions(receipt_id) WHERE receipt_id IS NOT NULL;
CREATE INDEX idx_transactions_recurring ON transactions(recurring_bill_id) WHERE recurring_bill_id IS NOT NULL;
CREATE INDEX idx_transactions_group ON transactions(group_expense_id) WHERE group_expense_id IS NOT NULL;
