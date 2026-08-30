-- Group Expense Service Schema
-- Tables: groups, group_members, shared_expenses, expense_splits, settlements

CREATE TABLE groups
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    created_by UUID         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP             DEFAULT NOW()
);

CREATE TABLE group_members
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    group_id     UUID         NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    user_id      UUID,         -- NULL for ghost members
    display_name VARCHAR(100) NOT NULL,
    is_ghost     BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (group_id, user_id) -- prevent duplicate real users
);

CREATE TABLE shared_expenses
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    group_id          UUID        NOT NULL REFERENCES groups (id),
    paid_by_member_id UUID        NOT NULL REFERENCES group_members (id),
    total_amount      BIGINT      NOT NULL,
    description       VARCHAR(500),
    receipt_id        UUID,
    split_method      VARCHAR(20) NOT NULL DEFAULT 'EQUAL',
    expense_date      DATE        NOT NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_split_method CHECK (split_method IN ('EQUAL', 'BY_PERCENTAGE', 'BY_EXACT_AMOUNT', 'BY_ITEM'))
);

CREATE TABLE expense_splits
(
    id                UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    shared_expense_id UUID      NOT NULL REFERENCES shared_expenses (id) ON DELETE CASCADE,
    member_id         UUID      NOT NULL REFERENCES group_members (id),
    amount            BIGINT    NOT NULL,
    item_name         VARCHAR(200), -- only for BY_ITEM split
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE settlements
(
    id             UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    group_id       UUID      NOT NULL REFERENCES groups (id),
    from_member_id UUID      NOT NULL REFERENCES group_members (id),
    to_member_id   UUID      NOT NULL REFERENCES group_members (id),
    amount         BIGINT    NOT NULL,
    settled_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_groups_created_by ON groups (created_by);
CREATE INDEX idx_group_members_group ON group_members (group_id);
CREATE INDEX idx_group_members_user ON group_members (user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_shared_expenses_group ON shared_expenses (group_id);
CREATE INDEX idx_expense_splits_expense ON expense_splits (shared_expense_id);
CREATE INDEX idx_settlements_group ON settlements (group_id);
