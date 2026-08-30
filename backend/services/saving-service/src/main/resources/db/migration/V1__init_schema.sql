-- Saving Service Schema
-- Tables: savings_goals, saving_contributions

CREATE TABLE savings_goals (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    target_amount               BIGINT NOT NULL,
    target_date                 DATE NOT NULL,
    contribution_frequency      VARCHAR(10) NOT NULL DEFAULT 'DAILY',
    current_saved_amount        BIGINT NOT NULL DEFAULT 0,
    status                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_saving_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_contribution_freq CHECK (contribution_frequency IN ('DAILY', 'WEEKLY')),
    CONSTRAINT chk_target_positive CHECK (target_amount > 0)
);

CREATE TABLE saving_contributions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    savings_goal_id     UUID NOT NULL REFERENCES savings_goals(id),
    amount              BIGINT NOT NULL,
    note                VARCHAR(500),
    contributed_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_contrib_positive CHECK (amount > 0)
);

-- Indexes
CREATE INDEX idx_savings_goals_user ON savings_goals(user_id);
CREATE INDEX idx_savings_goals_status ON savings_goals(user_id, status);
CREATE INDEX idx_saving_contributions_goal ON saving_contributions(savings_goal_id);
